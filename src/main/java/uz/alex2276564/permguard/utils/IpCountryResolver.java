package uz.alex2276564.permguard.utils;

import com.alibaba.fastjson2.JSONObject;
import lombok.experimental.UtilityClass;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@UtilityClass
public class IpCountryResolver {

    /**
     * Resolve player's country based on IP using a configurable HTTP JSON endpoint.
     * <p>
     * The endpoint template must contain the "{ip}" placeholder, which will be replaced
     * with the player's (sanitized) IP address. For example:
     * <p>
     * - https://free.freeipapi.com/api/v1/json/{ip}
     * - https://ipwho.is/{ip}
     * - https://api.example.com/lookup?key=YOUR_TOKEN&ip={ip}
     * - http://localhost/{ip}
     * <p>
     * The JSON response is expected to contain one of:
     * - countryName
     * - country
     * - country_name
     */
    public static String resolveCountry(String safeIp,
                                        String ipGeolocationEndpoint,
                                        HttpUtils httpUtils,
                                        MessagesConfig.TelegramMessagesSection tmsg,
                                        Logger logger) {

        try {
            String template = ipGeolocationEndpoint.trim();
            String encodedIp = URLEncoder.encode(safeIp, StandardCharsets.UTF_8);
            String urlString = template.replace("{ip}", encodedIp);

            HttpUtils.HttpResponse response = httpUtils.getJson(urlString, null);

            if (response.isOk()) {
                JSONObject json = response.jsonBody();

                // Try common country field names used by several providers.
                String country = null;
                if (json.containsKey("countryName")) {
                    country = json.getString("countryName"); // freeipapi.com style
                } else if (json.containsKey("country")) {
                    country = json.getString("country");     // ipwho.is, ip-api.com, etc.
                } else if (json.containsKey("country_name")) {
                    country = json.getString("country_name");
                }

                if (country != null && !country.isEmpty()) {
                    return SecurityUtils.sanitize(country, SecurityUtils.SanitizeType.COUNTRY);
                }

                // 2xx but no usable country field
                String reason = "response did not contain a recognizable country field";
                logFailure(safeIp, reason, tmsg, logger);
            } else {
                String reason = response.hasError()
                        ? response.describeError()
                        : "HTTP " + response.statusCode();
                logFailure(safeIp, reason, tmsg, logger);
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

    private static void logFailure(String safeIp,
                                   String reason,
                                   MessagesConfig.TelegramMessagesSection tmsg,
                                   Logger logger) {
        String msg = tmsg.countryLookupFailed
                .replace("<ip>", safeIp)
                .replace("<error>", SecurityUtils.sanitize(
                        reason,
                        SecurityUtils.SanitizeType.ERROR_MESSAGE
                ));
        logger.warning(msg);
    }
}