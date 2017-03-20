package six.com.crawler.common.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * @author six
 * @date 2016年5月20日 下午3:19:33
 */
public class JsonUtils {

	/**
	 * 通過java 類加載器 實現懶加載
	 * 
	 * @author six
	 * @email 359852326@qq.com
	 */
	static class ProxyGson {
		private static Gson GSON = new Gson();
		static {
			/**
			 * 在使用Gson进行转换时，使用Object存储时，会将int转换为double类型，比如100会转换为100.0。解决办法
			 * 对Gson进行一些配置，解析时使用这个gson对象即可 !
			 */
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Map.class, new MapJsonDeserializer());
			GSON = gsonBuilder.create();

		}
	}

	static class MapJsonDeserializer implements JsonDeserializer<Map<String, Object>> {
		@Override
		public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			Map<String, Object> treeMap = new HashMap<>();
			JsonObject jsonObject = json.getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
			for (Map.Entry<String, JsonElement> entry : entrySet) {
				String tempKey = entry.getKey();
				JsonElement tempJsonElement = entry.getValue();
				Object object = paserJsonElement(tempJsonElement);
				treeMap.put(tempKey, object);
			}
			return treeMap;
		}
	}

	static Object paserJsonElement(JsonElement jsonElement) {
		if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Map<String, Object> tempMap = new HashMap<>();
			Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
			for (Map.Entry<String, JsonElement> entry : entrySet) {
				String tempKey = entry.getKey();
				JsonElement tempJsonElement = entry.getValue();
				Object tempObject = paserJsonElement(tempJsonElement);
				tempMap.put(tempKey, tempObject);
			}
			return tempMap;
		} else if (jsonElement.isJsonArray()) {
			JsonArray JsonArray = jsonElement.getAsJsonArray();
			Iterator<JsonElement> jsonArrayIterator = JsonArray.iterator();
			List<Object> tempList = new ArrayList<>();
			while (jsonArrayIterator.hasNext()) {
				JsonElement tempJsonElement = jsonArrayIterator.next();
				Object tempObject = paserJsonElement(tempJsonElement);
				tempList.add(tempObject);
			}
			return tempList;
		} else if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
			if (primitive.isNumber()) {
				if (primitive.getAsInt() == primitive.getAsDouble()) {
					return primitive.getAsInt();
				} else {
					return primitive.getAsDouble();
				}
			} else if (primitive.isBoolean()) {
				return primitive.getAsBoolean();
			} else {
				return primitive.getAsString();
			}
		} else {
			return null;
		}
	}

	public static Gson getGson() {
		return ProxyGson.GSON;
	}

	public static String toJson(Object ob) {
		if (null == ob) {
			throw new RuntimeException("this ob must not null");
		}
		return getGson().toJson(ob);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> toMap(Object ob) {
		if (null == ob) {
			throw new RuntimeException("this ob must not null");
		}
		String json=getGson().toJson(ob);
		Map<String,Object> map=toObject(json, Map.class);
		return map;
	}

	public static <T> T toObject(String json, Class<T> clz) {
		if (StringUtils.isBlank(json)) {
			throw new RuntimeException("this json must not be blank");
		}
		// 替换掉json里带有 单个反斜杠 避免转换异常
		json = StringUtils.replace(json, "\\", "\\\\");
		TypeToken<T> typeToken = TypeToken.of(clz);
		return getGson().fromJson(json.toString(), typeToken.getType());
	}
	
	public static <T> T mapToObject(Map<String, Object> map, Class<T> clz) {
		String json=toJson(map);
		TypeToken<T> typeToken = TypeToken.of(clz);
		return getGson().fromJson(json, typeToken.getType());
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String json = "{\"rn\": \"商铺45\",\"y\": 3,\"x\": 45,\"arras\":[a,b,c],\"object\":{\"name\":[a,b,c]}}";
		Map<String, Object> resultMap = JsonUtils.toObject(json, Map.class);
		for (String key : resultMap.keySet()) {
			Object result = resultMap.get(key);
			System.out.println(result);
		}
		System.out.println(resultMap.get("rn").getClass());
		System.out.println(resultMap.get("arras").getClass());
		System.out.println(resultMap.get("object").getClass());
		Map<String, Object> object = (Map<String, Object>) resultMap.get("object");
		System.out.println(object.get("name").getClass());
	}

}
