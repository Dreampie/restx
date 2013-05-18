package restx.tests;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.MatcherAssert;
import restx.factory.Component;
import restx.specs.RestxSpec;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static restx.specs.RestxSpec.WhenHttpRequest.BASE_URL;

@Component
public class WhenHttpChecker implements WhenChecker<RestxSpec.WhenHttpRequest> {

    @Override
    public Class<RestxSpec.WhenHttpRequest> getWhenClass() {
        return RestxSpec.WhenHttpRequest.class;
    }

    @Override
    public void check(RestxSpec.WhenHttpRequest when, ImmutableMap<String, String> params) {
        Stopwatch stopwatch = new Stopwatch().start();
        String url = checkNotNull(params.get(BASE_URL),
                BASE_URL + " param is required") + "/" + when.getPath();
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println(">> REQUEST");
        System.out.println(when.getMethod() + " " + url);
        System.out.println();
        HttpRequest httpRequest = new HttpRequest(url, when.getMethod());

        if (!when.getCookies().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : when.getCookies().entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            sb.setLength(sb.length() - 2);
            httpRequest.header("Cookie", sb.toString());
        }

        if (!Strings.isNullOrEmpty(when.getBody())) {
            httpRequest.contentType("application/json");
            httpRequest.send(when.getBody());
            System.out.println(when.getBody());
        }
        System.out.println();

        int code = httpRequest.code();
        System.out.println("<< RESPONSE");
        System.out.println(code);
        System.out.println();
        String body = httpRequest.body(Charsets.UTF_8.name());
        System.out.println(body);
        System.out.println();

        assertThat(code).isEqualTo(when.getThen().getExpectedCode());
        MatcherAssert.assertThat(body,
                SameJSONAs.sameJSONAs(when.getThen().getExpected()).allowingExtraUnexpectedFields());
        System.out.printf("checked %s /%s -- %s%n", when.getMethod(), when.getPath(), stopwatch.stop().toString());
    }
}
