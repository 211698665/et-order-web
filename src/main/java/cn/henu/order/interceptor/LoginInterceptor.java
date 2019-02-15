package cn.henu.order.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import cn.henu.cart.service.CartService;
import cn.henu.common.utils.CookieUtils;
import cn.henu.common.utils.EtResult;
import cn.henu.common.utils.JsonUtils;
import cn.henu.pojo.TbItem;
import cn.henu.pojo.TbUser;
import cn.henu.sso.service.TokenService;
//用户登录拦截器
public class LoginInterceptor implements HandlerInterceptor{

	@Autowired
	private TokenService tokenService;
	@Autowired
	private CartService cartService;
	@Value("${SSO_URL}")
	private String SSO_URL;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//判断用户是否登录
		//从cookie中取token,判断token是否存在
		String token = CookieUtils.getCookieValue(request, "token");
		if(StringUtils.isBlank(token)) {
			//如果不存在，未登录状态，跳转到登录页面,登录成功后跳转回来
			//注意这里只能使用redirect跳转，因为没有在同一个工程，所以不能使用forward
			response.sendRedirect(SSO_URL+"/page/login?redirect="+request.getRequestURL());
			//拦截
			return false;
		}
		//如果存在，调用sso系统的服务，根据token取用户信息，如果取不到，说明登录过期，提示用户登录
		EtResult etResult = tokenService.getUserBytoken(token);
		if(etResult.getStatus()!=200) {
			//如果不存在，未登录状态，跳转到登录页面,登录成功后跳转回来
			//注意这里只能使用redirect跳转，因为没有在同一个工程，所以不能使用forward
			response.sendRedirect(SSO_URL+"/page/login?redirect="+request.getRequestURI());
			//拦截
			return false;
		}
		//如果取到用户信息，说明用户登录，这时需要把用户信息写到request
		TbUser user = (TbUser) etResult.getData();
		request.setAttribute("user", user);
		//判断cookie有没有购物车数据，如果有就合并到服务端
		String json = CookieUtils.getCookieValue(request, "cart",true);
		if(StringUtils.isNotBlank(json)) {
			//合并购物车
			cartService.mergeCart(user.getId(), JsonUtils.jsonToList(json, TbItem.class));
		}
		//放行
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	
}
