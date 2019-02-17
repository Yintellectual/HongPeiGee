package com.spDeveloper.hongpajee.aliyun;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.DeleteVideoRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class ApsaraEmbassador {

	@Value("${aliyun.ram.AccessKeyID}")
	private String ALIYUN_RAM_ACCESS_KEY_ID;
	@Value("${aliyun.ram.AccessKeySecret}")
	private String ALIYUN_RAM_ACCESS_KEY_SECRET;
	@Value("${aliyun.live.room}")
	private String room;
	private ApsaraLiveChannel apsaraLiveChannel;
	
	@Autowired
	DefaultAcsClient defaultAcsClient;
	@Bean
	DefaultAcsClient defaultAcsClient() {
		String regionId = "cn-shanghai";
		System.out.println("acs_client"+ALIYUN_RAM_ACCESS_KEY_ID);
		System.out.println("acs_client"+ALIYUN_RAM_ACCESS_KEY_SECRET);
		DefaultProfile profile = DefaultProfile.getProfile(regionId, ALIYUN_RAM_ACCESS_KEY_ID,
				ALIYUN_RAM_ACCESS_KEY_SECRET);
		DefaultAcsClient client = new DefaultAcsClient(profile);
		return client;
	}
	
	@PostConstruct
	public void init() {
		apsaraLiveChannel = new ApsaraLiveChannel("HongPeiGee", room);
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ApsaraLiveStream{
		private String pushURL;
		private String streamKey;
		private String pullURL;
		private String pullURL4flv;
		private String pullURL4m3u8;
		
		public JsonObject toJsonObject() {
			JsonObject result = new JsonObject();
			result.add("pushURL", new JsonPrimitive(pushURL));
			result.add("streamKey", new JsonPrimitive(streamKey));
			result.add("pullURL", new JsonPrimitive(pullURL));
			result.add("pullURL4flv", new JsonPrimitive(pullURL4flv));
			result.add("pullURL4m3u8", new JsonPrimitive(pullURL4m3u8));
			return result; 
		}
	}
	
	
	private ApsaraLiveStream apsaraLiveStream = apsaraLiveChannel.getApsaraLiveStream();
	
	@Scheduled(cron="10 0/20 * * * *")
	public ApsaraLiveStream getNewApsaraLiveStream() {
		ApsaraLiveStream newApsaraLiveStream = apsaraLiveChannel.getApsaraLiveStream();
		this.apsaraLiveStream = newApsaraLiveStream;
		return apsaraLiveStream;
	}
	public ApsaraLiveStream getCurrentApsaraLiveStream() {
		return apsaraLiveStream;
	}
	public String getCurrentLivePullURL() {
		return this.apsaraLiveStream.getPullURL();
	}
	
	public CreateUploadVideoResponse getCreateUploadVideoResponse(String title, String fileName, JsonObject extension)
			throws ServerException, ClientException {
		CreateUploadVideoRequest request = new CreateUploadVideoRequest();
		request.setTitle(title);
		request.setFileName(fileName);
		JsonObject userData = new JsonObject();
		JsonObject messageCallback = new JsonObject();
		messageCallback.add("CallbackURL", new JsonPrimitive("http://xxxxx"));
		messageCallback.add("CallbackType", new JsonPrimitive("http"));
		userData.add("MessageCallback", messageCallback);
		userData.add("Extend", extension);

		return defaultAcsClient.getAcsResponse(request);
	}

	public GetVideoPlayAuthResponse getVideoPlayAuthResponse(String videoId) throws ServerException, ClientException {
		GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
		request.setVideoId(videoId);
		return defaultAcsClient.getAcsResponse(request);
	}

	public GetVideoInfoResponse getVideoInfoRequest(String videoId) throws ServerException, ClientException {
		GetVideoInfoRequest request = new GetVideoInfoRequest();
		request.setVideoId(videoId);
		return defaultAcsClient.getAcsResponse(request);
	}

	public void deleteVideos(List<String> videoIds) throws ServerException, ClientException {
		if (Objects.isNull(videoIds)) {
			return;
		}
		DeleteVideoRequest request = new DeleteVideoRequest();
		// 支持传入多个视频ID，多个用逗号分隔
		request.setVideoIds(Strings.join(videoIds, ','));
		defaultAcsClient.getAcsResponse(request);
	}

	@Data
	public static class ApsaraLiveChannel {
		private static final String ALI_LIVE_CENTER = "video-center.alivecdn.com";
		private String pullDomainName = "upstream.hanchen.site";
		private String pushDomainName = "downstream.hanchen.site";
		private String protocal = "rtmp";
		private String appName;
		private String streamName;
		private int validPeriodInMunite = 30;
		private String pushPrivateKey = "OGntJxTknk";
		private String pullPrivateKey = "cgwv4dzbG8";
		
		public ApsaraLiveChannel(String appName, String streamName) {
			this.appName = appName;
			this.streamName = streamName;
		}
		
		private String auth_key(String app, String stream, String privateKey) {
			int deadLine = deadLine();
			int random = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
			int uid = 0;
			String hashValue = md5Encrypt(app, stream, deadLine, random, uid, privateKey);
			return String.format("%d-%d-%d-%s", deadLine, random, uid, hashValue);
		}

		private int deadLine() {
			int now_epoch = (int) (Instant.now().toEpochMilli()/1000);
			int validPeriodInSecond = validPeriodInMunite * 60;
			return now_epoch + validPeriodInSecond;
		}

		private String md5Encrypt(String app, String stream, int timestamp, int rand, int uid, String privateKey) {
			/// {AppName}/{StreamName}-{timestamp}-{rand}-{uid}-{privatekey};
			String data = String.format("/%s/%s-%d-%d-%d-%s", app, stream,timestamp, rand, uid, privateKey);
			return DigestUtils.md5Hex(data).toLowerCase();
		}

		public ApsaraLiveStream getApsaraLiveStream() {
			StringBuilder pushURL = new StringBuilder();
			pushURL.append(protocal);
			pushURL.append("://");
			pushURL.append(pushDomainName);
			pushURL.append("/");
			pushURL.append(appName);
			pushURL.append("/");
			
			StringBuilder streamKey = new StringBuilder();
			streamKey.append(streamName).append("?auth_key=").append(auth_key(appName,streamName,pushPrivateKey));
			
			StringBuilder pullURL = new StringBuilder();
			pullURL.append(protocal).append("://").append(pullDomainName).append("/").append(appName).append("/")
					.append(streamName).append("?auth_key=").append(auth_key(appName,streamName,pullPrivateKey));
			StringBuilder pullURL4flv = new StringBuilder();
			pullURL4flv.append("http").append("://").append(pullDomainName).append("/").append(appName).append("/")
					.append(streamName+".flv").append("?auth_key=").append(auth_key(appName,streamName+".flv",pullPrivateKey));
			StringBuilder pullURL4m3u8 = new StringBuilder();
			pullURL4m3u8.append("http").append("://").append(pullDomainName).append("/").append(appName).append("/")
					.append(streamName+".m3u8").append("?auth_key=").append(auth_key(appName,streamName+".m3u8", pullPrivateKey));
			
			return new ApsaraLiveStream(pushURL.toString(), streamKey.toString(), pullURL.toString(), pullURL4flv.toString(), pullURL4m3u8.toString());
		}
		public JsonObject getJsonObject() {
			return getApsaraLiveStream().toJsonObject();
		}
	}
}
