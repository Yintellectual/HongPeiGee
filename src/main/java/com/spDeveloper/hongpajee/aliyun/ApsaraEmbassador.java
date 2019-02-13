package com.spDeveloper.hongpajee.aliyun;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
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

	@Autowired
	DefaultAcsClient defaultAcsClient;

	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ApsaraLiveStream{
		private String pushURL;
		private String streamKey;
		private String pullURL;
		
		public JsonObject toJsonObject() {
			JsonObject result = new JsonObject();
			result.add("pushURL", new JsonPrimitive(pushURL));
			result.add("streamKey", new JsonPrimitive(streamKey));
			result.add("pullURL", new JsonPrimitive(pullURL));
			return result; 
		}
	}
	
	private ApsaraLiveChannel apsaraLiveChannel = new ApsaraLiveChannel("main", "000");
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
		
		private String auth_key(String privateKey) {
			int deadLine = deadLine();
			int random = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
			int uid = 0;
			String hashValue = md5Encrypt(deadLine, random, uid, privateKey);
			return String.format("%d-%d-%d-%s", deadLine, random, uid, hashValue);
		}

		private int deadLine() {
			int now_epoch = (int) (Instant.now().toEpochMilli()/1000);
			int validPeriodInSecond = validPeriodInMunite * 60;
			return now_epoch + validPeriodInSecond;
		}

		private String md5Encrypt(int timestamp, int rand, int uid, String privateKey) {
			/// {AppName}/{StreamName}-{timestamp}-{rand}-{uid}-{privatekey};
			String data = String.format("/%s/%s-%d-%d-%d-%s", appName, streamName,timestamp, rand, uid, privateKey);
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
			streamKey.append(streamName).append("?auth_key=").append(auth_key(pushPrivateKey));
			
			StringBuilder pullURL = new StringBuilder();
			pullURL.append(protocal).append("://").append(pullDomainName).append("/").append(appName).append("/")
					.append(streamName).append("?auth_key=").append(auth_key(pullPrivateKey));
			return new ApsaraLiveStream(pushURL.toString(), streamKey.toString(), pullURL.toString());
		}
		public JsonObject getJsonObject() {
			return getApsaraLiveStream().toJsonObject();
		}
	}
}
