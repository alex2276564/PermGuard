package uz.alex2276564.permguard.utils;

import com.alibaba.fastjson2.JSONObject;
import lombok.experimental.UtilityClass;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;

import java.util.logging.Logger;

@UtilityClass
public class IpCountryResolver {

    // SECURITY NOTE: Plain HTTP is used because ip-api.com requires a paid subscription for HTTPS access.
    // MITM exposure is accepted here as these geolocation data points are non-critical and
    // strictly validated via SecurityUtils to prevent any injection vectors.
    @SuppressWarnings("HttpUrlsUsage")
    private static final String IP_API_URL = "http://ip-api.com/json/%s";

    public static String resolveCountry(String safeIp,
                                        HttpUtils httpUtils,
                                        MessagesConfig.TelegramMessagesSection tmsg,
                                        Logger logger) {

        try {
            String urlString = String.format(IP_API_URL, safeIp);

            HttpUtils.HttpResponse response = httpUtils.getJson(urlString, null);

            if (response.statusCode() == 200) {
                JSONObject json = response.jsonBody();
                if (json.containsKey("country")) {
                    String country = json.getString("country");
                    return SecurityUtils.sanitize(country, SecurityUtils.SanitizeType.COUNTRY);
                }
            }
        } catch (Exception e) {
            String msg = tmsg.countryLookupFailed
                    .replace("<ip>", safeIp)
                    .replace("<error>", SecurityUtils.sanitize(
                            e.getMessage(),
                            SecurityUtils.SanitizeType.ERROR_MESSAGE
                    ));
            logger.warning(msg);
        }
        return tmsg.unknownCountry;
    }
}