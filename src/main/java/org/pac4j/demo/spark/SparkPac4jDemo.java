package org.pac4j.demo.spark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pac4j.core.config.Config;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.sparkjava.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import static spark.Spark.*;

public class SparkPac4jDemo {

	private final static String JWT_SALT = "12345678901234567890123456789012";

	private final static Logger logger = LoggerFactory.getLogger(SparkPac4jDemo.class);

	private final static MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

	public static void main(String[] args) {
		port(8080);
		final Config config = new DemoConfigFactory(JWT_SALT, templateEngine).build();

		get("/", SparkPac4jDemo::index, templateEngine);
		final CallbackRoute callback = new CallbackRoute(config, null, true);
		//callback.setRenewSession(false);
		get("/callback", callback);
		post("/callback", callback);
		before("/login", new SecurityFilter(config, "IndirectBasicAuthClient"));
		before("/auth", new SecurityFilter(config, "ParameterClient"));
		get("/login", SparkPac4jDemo::protectedIndex, templateEngine);
		get("/jwt", SparkPac4jDemo::jwt, templateEngine);
		get("/auth", SparkPac4jDemo::protectedIndex, templateEngine);

		final LogoutRoute localLogout = new LogoutRoute(config, "/?defaulturlafterlogout");
		localLogout.setDestroySession(true);
		get("/logout", localLogout);

		exception(Exception.class, (e, request, response) -> {
			logger.error("Unexpected exception", e);
			response.body(templateEngine.render(new ModelAndView(new HashMap<>(), "error500.mustache")));
		});
    }

	private static ModelAndView index(final Request request, final Response response) {
		final Map map = new HashMap();
		map.put("profiles", getProfiles(request, response));
		final SparkWebContext ctx = new SparkWebContext(request, response);
		map.put("sessionId", ctx.getSessionStore().getOrCreateSessionId(ctx));
		return new ModelAndView(map, "index.mustache");
	}

	private static ModelAndView jwt(final Request request, final Response response) {
		final SparkWebContext context = new SparkWebContext(request, response);
		final ProfileManager manager = new ProfileManager(context);
		final Optional<CommonProfile> profile = manager.get(true);
		String token = "";
		if (profile.isPresent()) {
			JwtGenerator generator = new JwtGenerator(new SecretSignatureConfiguration(JWT_SALT));
			token = generator.generate(profile.get());
		}
		final Map map = new HashMap();
		map.put("token", token);
		return new ModelAndView(map, "jwt.mustache");
	}

	private static ModelAndView protectedIndex(final Request request, final Response response) {
		final Map map = new HashMap();
		map.put("profiles", getProfiles(request, response));
		return new ModelAndView(map, "protectedIndex.mustache");
	}

	private static List<CommonProfile> getProfiles(final Request request, final Response response) {
		final SparkWebContext context = new SparkWebContext(request, response);
		final ProfileManager manager = new ProfileManager(context);
		return manager.getAll(true);
	}
}
