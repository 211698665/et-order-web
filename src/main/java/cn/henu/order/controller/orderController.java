package cn.henu.order.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.henu.cart.service.CartService;
import cn.henu.common.utils.EtResult;
import cn.henu.order.pojo.OrderInfo;
import cn.henu.order.service.OrderService;
import cn.henu.pojo.TbItem;
import cn.henu.pojo.TbUser;

/**
 * 订单管理
 * @author syw
 *
 */
@Controller
public class orderController {

	@Autowired
	private CartService cartService;
	@Autowired
	private OrderService orderService;
	@RequestMapping("/order/order-cart")
	public String showOrderCart(HttpServletRequest request) {
		//取出购物车列表,根据用户id
		TbUser user = (TbUser) request.getAttribute("user");
		List<TbItem> cartList = cartService.getCartList(user.getId());
		//根据用户ID取出收货地址列表，在这里因为没有那个表，所以使用静态数据
		//取支付方式列表,这里也是静态的数据
		
		//把购物车列表传递给jsp
		request.setAttribute("cartList", cartList);
		//返回页面
		return "order-cart";
	}
	
	@RequestMapping(value="/order/create",method=RequestMethod.POST)
	public String createOrder(OrderInfo orderInfo,HttpServletRequest request) {
		//取用户信息
		TbUser user = (TbUser) request.getAttribute("user");
		//把用户信息添加到orderInfo中
		orderInfo.setUserId(user.getId());
		orderInfo.setBuyerNick(user.getUsername());
		//调用服务生成订单
		EtResult etResult = orderService.createOrder(orderInfo);
		//如果订单生成成功，删除购物车
		if(etResult.getStatus()==200) {
			//清空购物车
			cartService.clearCartItem(user.getId());
		}
		//把订单号传递给页面
		request.setAttribute("orderId", etResult.getData());
		request.setAttribute("payment", orderInfo.getPayment());
		//返回一个逻辑视图
		return "success";
	}
}
