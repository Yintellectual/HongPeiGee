package com.spDeveloper.hongpajee.user.config;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer.UserDetailsBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.user.entity.UserRole;

/**
 * 1. Encode data and save in redis 2. Decode things from redis and restore the
 * data 3. Do restoration after restart 4. Any change of data is applied for the
 * database immediately 5. After any change of data, the change is also made to
 * the in-memory data storage 6. any read of data goes to the in-memory data
 * storage and never to the database. 7. for details other than username and
 * password, use ProfileRepository
 * 
 */
// its a bean registered in WebSecurityConfig
@Service
public class RedisUserDetailsManager implements UserDetailsManager, UserDetailsPasswordService, AuthenticationProvider {

	private final Map<String, UserDetails> users = new ConcurrentHashMap();
	private final Set<String> admins = new ConcurrentSkipListSet<String>();
	private final Set<String> owners = new ConcurrentSkipListSet<>();
	@Autowired
	StringRedisTemplate stringRedisTemplate;

	private final String USER_NAME_SET = "UDM:set:usernames";
	private final String USERNAME_PASSWORD_HASH = "UDM:hash:passwords";
	private final String USERNAME_ROLES_HASH = "UDM:hash:roles";

	private SetOperations<String, String> setOperations;
	private HashOperations<String, String, String> hashOperations;
	private ListOperations<String, String> listOperations;

	Lock redisLock = new ReentrantLock();
	@Value("${aes.key}")
	private String stringKey;
	@Value("${aes.cbc.iv}")
	private String stringIv;

	private Cipher aesCipher;
	private SecretKey secretKey;
	private IvParameterSpec ivParameterSpec;

	@PostConstruct
	public void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
		setOperations = stringRedisTemplate.opsForSet();
		hashOperations = stringRedisTemplate.opsForHash();
		listOperations = stringRedisTemplate.opsForList();
		aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		final int AES_KEYLENGTH = 128;
		byte[] decodedKey = Base64.getDecoder().decode(stringKey);
		secretKey = new SecretKeySpec(decodedKey, "AES");
		byte[] iv = Base64.getDecoder().decode(stringIv);
		ivParameterSpec = new IvParameterSpec(iv);
		restore();
	}

	/*
	 * An independent data structure in redis is used to backup the
	 * username-password-roles information. The SpringBootApplication will restore
	 * from the redis database on startup. All changes of data is reflected on the
	 * redis database first, then the application memory. The data in redis is
	 * encrypted with "AES/CBC/PKCS5Padding"
	 * 
	 * The data structure in redis is: set of usernames: "UDM:set:usernames"
	 * {username} hash of passwords: "UDM:hash:passwords" "{username}"
	 * {encrypted(password)} hash of roles: "UDM:hash:roles" "{username}"
	 * {encrypted(roles seperated by ',')}
	 */

	private void restore() {
		Set<String> usernames = setOperations.members(USER_NAME_SET);
		if (usernames == null || usernames.isEmpty()) {
			return;
		}
		redisLock.lock();
		try {
			usernames.stream().map(un -> {
				String password;
				try {
					password = decode(hashOperations.get(USERNAME_PASSWORD_HASH, un));
				} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
						| InvalidAlgorithmParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				Set<GrantedAuthority> authorities;
				try {
					authorities = stringToRoles(decode(hashOperations.get(USERNAME_ROLES_HASH, un)));
				} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
						| InvalidAlgorithmParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return User.withUsername(un).password(password).authorities(authorities).build();
			}).forEach(ud -> {
				createUser(ud);
			});
		} finally {
			redisLock.unlock();
		}
	}

	private void forceSave(UserDetails user) {
		redisLock.lock();
		try {
			setOperations.add(USER_NAME_SET, user.getUsername());
			hashOperations.put(USERNAME_PASSWORD_HASH, user.getUsername(), encode(user.getPassword()));
			hashOperations.put(USERNAME_ROLES_HASH, user.getUsername(), encode(rolesToString(user.getAuthorities())));
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			redisLock.unlock();
		}
	}

	private void forceDelete(String username) {
		redisLock.lock();
		try {
			setOperations.remove(USER_NAME_SET, username);
			hashOperations.delete(USERNAME_PASSWORD_HASH, username);
			hashOperations.delete(USERNAME_ROLES_HASH, username);
		} finally {
			redisLock.unlock();
		}
	}

	private String encode(String string) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException {
		byte[] raw = string.getBytes();
		byte[] encrypted = encrypt(raw);
		return base64Encoding(encrypted);
	}

	private String decode(String string) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException {
		byte[] encrypted = base64Decoding(string);
		byte[] raw = decrypt(encrypted);
		return new String(raw);
	}

	private String rolesToString(Collection<? extends GrantedAuthority> collection) {
		StringBuilder sb = new StringBuilder();
		collection.forEach(ga -> {
			sb.append(ga.getAuthority()).append(',');
		});
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	private Set<GrantedAuthority> stringToRoles(String string) {
		Set<GrantedAuthority> roles = new HashSet<>();
		return Arrays.stream(string.split(",+")).filter(s -> s != null).filter(s -> !s.isEmpty())
				.map(s -> new SimpleGrantedAuthority(s)).collect(Collectors.toSet());
	}

	// raw string->raw bytes->encrypt->base64 encoded string->base 64 decoded
	// bytes->decrypt->recoverd bytes->recovered string == raw string
	private String base64Encoding(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	private byte[] base64Decoding(String string) {
		return Base64.getDecoder().decode(string);
	}

	private byte[] encrypt(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException {

		aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
		return aesCipher.doFinal(plaintext);
	}

	private byte[] decrypt(byte[] encrypted) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException {

		aesCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
		return aesCipher.doFinal(encrypted);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		if (Objects.isNull(username) || !userExists(username)) {
			return null;
		} else {
			UserDetails record = users.get(username);
			return User.withUsername(record.getUsername()).authorities(record.getAuthorities()).password("").build();
		}
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		// TODO Auto-generated method stub
		String username = user.getUsername();
		UserDetails newUser = User.withUsername(username).password(newPassword).authorities(user.getAuthorities())
				.build();
		updateUser(newUser);
		return user;
	}

	public void updateRole(String username, UserRole newRole) {
		UserDetails oldUser = users.get(username);
		UserDetails newUser = User.withUsername(username).password(oldUser.getPassword())
				.authorities(newRole.getAuthorities()).build();
		updateUser(newUser);
	}

	@Override
	public void createUser(UserDetails user) {
		if (user == null) {
			// do nothing
			return;
		}
		if (userExists(user.getUsername())) {

		} else {
			forceSave(user);
			users.putIfAbsent(user.getUsername(), user);
			if (hasRole(user, UserRole.ROLE_OWNER)) {
				owners.add(user.getUsername());
			} else if (hasRole(user, UserRole.ROLE_ADMIN)) {
				admins.add(user.getUsername());
			}
		}
	}

	private boolean hasRole(UserDetails user, UserRole role) {
		return user.getAuthorities().stream().map(ga -> {
			return ga.getAuthority();
		}).anyMatch(r -> {
			return r.equals(role.toString());
		});
	}

	@Override
	public void updateUser(UserDetails user) {
		// TODO Auto-generated method stub
		if (Objects.isNull(user)) {
			return;
		} else {
			if (userExists(user.getUsername())) {
				deleteUser(user.getUsername());
				createUser(user);
			} else {
				// do nothing
			}
		}
	}

	@Override
	public void deleteUser(String username) {
		// TODO Auto-generated method stub
		if (Objects.isNull(username)) {
			return;
		} else {
			forceDelete(username);
			users.remove(username);
			admins.remove(username);
			owners.remove(username);
		}
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		// TODO Auto-generated method stub
		SecurityContext secureContext = SecurityContextHolder.getContext();
		Authentication auth = secureContext.getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			return;
		}
		auth = authenticate(
				new UsernamePasswordAuthenticationToken(auth.getName(), oldPassword, auth.getAuthorities()));
		if (auth != null) {
			// if password if good
			String username = auth.getName();
			String password = newPassword;
			Set<GrantedAuthority> authorities = new HashSet<>(auth.getAuthorities());
			UserDetails newUserDetails = User.withUsername(username).password(password).authorities(authorities)
					.build();
			updateUser(newUserDetails);
		} else {

		}
	}

	@Override
	public boolean userExists(String username) {
		// TODO Auto-generated method stub
		if (username == null || username.isEmpty()) {
			return false;
		} else {
			return users.containsKey(username);
		}
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// TODO Auto-generated method stub
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		UserDetails record = users.get(username);
		if (record.getPassword().equals(password)) {
			return new UsernamePasswordAuthenticationToken(record.getUsername(), record.getPassword(),
					record.getAuthorities());
		} else {
			return null;
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	public List<UserDetails> loadUserByRole(UserRole role) {

		List<UserDetails> result = new ArrayList<>();

		if (role == UserRole.ROLE_OWNER) {
			for (String username : owners) {
				result.add(hidePassword(users.get(username)));
			}
		} else if (role == UserRole.ROLE_ADMIN) {
			for (String username : admins) {
				result.add(hidePassword(users.get(username)));
			}
		} else if (role == UserRole.ROLE_USER) {
			Set<String> userspace = new HashSet<>(users.keySet());
			userspace.removeAll(admins);
			userspace.removeAll(owners);
			for (String username : userspace) {
				result.add(hidePassword(users.get(username)));
			}
		}
		Collections.sort(result, (u1, u2) -> {
			return u1.getUsername().compareTo(u2.getUsername());
		});

		return result;
	}

	private UserDetails hidePassword(UserDetails userDetails) {
		return User.withUsername(userDetails.getUsername()).authorities(userDetails.getAuthorities()).password("")
				.build();
	}
}
