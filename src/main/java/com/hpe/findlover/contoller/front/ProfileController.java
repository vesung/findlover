package com.hpe.findlover.contoller.front;

import com.hpe.findlover.model.*;
import com.hpe.findlover.service.*;
import com.hpe.findlover.service.front.ComplainService;
import com.hpe.findlover.service.front.FollowService;
import com.hpe.findlover.util.LoverUtil;
import com.hpe.findlover.util.SessionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@RequestMapping("profile")
public class ProfileController {
	private Logger logger = LogManager.getLogger(ProfileController.class);
	private final UserService userBasicService;
	private final UserAssetService userAssetService;
	private final UserDetailService userDetailService;
	private final UserLifeService userLifeService;
	private final UserStatusService userStatusService;
	private final UserPickService userPickService;
	private final LabelService labelService;
	private final ComplainService complainService;
	private final FollowService followService;

	@Autowired
	public ProfileController(UserService userBasicService, UserAssetService userAssetService, UserDetailService userDetailService, UserLifeService userLifeService, UserStatusService userStatusService, UserPickService userPickService, LabelService labelService, ComplainService complainService, FollowService followService) {
		this.userBasicService = userBasicService;
		this.userAssetService = userAssetService;
		this.userDetailService = userDetailService;
		this.userLifeService = userLifeService;
		this.userStatusService = userStatusService;
		this.userPickService = userPickService;
		this.labelService = labelService;
		this.complainService = complainService;
		this.followService = followService;
	}

	@GetMapping("{id}")
	public String getProfileById(@PathVariable("id") int userId, Model model,HttpServletRequest request) {
		UserBasic basic = userBasicService.selectByPrimaryKey(userId);
		if (basic.getAuthority() == 0) {
			logger.debug("ID为" + basic.getId() + "的用户个人资料所有人不可见");
		} else if (basic.getAuthority() == 2) {
			logger.debug("ID为" + basic.getId() + "的用户个人资料仅关注可见");
		}
		basic.setAge(LoverUtil.getAge(basic.getBirthday()));
		UserAsset asset = userAssetService.selectByPrimaryKey(userId);
		if (asset != null) {
			basic.setVip(LoverUtil.getDiffOfHours(asset.getVipDeadline()) > 0);
			basic.setStar(LoverUtil.getDiffOfHours(asset.getStarDeadline()) > 0);
		}
		model.addAttribute("basic", LoverUtil.prettyDisplay(basic,UserBasic.class));
		model.addAttribute("detail", LoverUtil.prettyDisplay(userDetailService.selectByPrimaryKey(userId), UserDetail.class));
		model.addAttribute("life", LoverUtil.prettyDisplay(userLifeService.selectByPrimaryKey(userId),UserLife.class));
		model.addAttribute("pick", LoverUtil.prettyDisplay(userPickService.selectByPrimaryKey(userId),UserPick.class));
		model.addAttribute("status", LoverUtil.prettyDisplay(userStatusService.selectByPrimaryKey(userId),UserStatus.class));
		model.addAttribute("code", basic.getAuthority());
		Follow follow = new Follow();
		follow.setUserId(SessionUtils.getSessionAttr(request,"user",UserBasic.class).getId());
		follow.setFollowId(basic.getId());
		model.addAttribute("isFollow", followService.selectOne(follow) != null);
		return "front/view_profile";
	}

	@PostMapping("complain")
	@ResponseBody
	public boolean complain(Complain complain, HttpServletRequest request) {
		complain.setUserId(SessionUtils.getSessionAttr(request, "user", UserBasic.class).getId());
		complain.setComTime(new Date());
		complain.setStatus(0);
		return complainService.insert(complain);
	}

}