package com.kbs.www.controllers;

import com.kbs.www.entities.WriteEntity;
import com.kbs.www.entities.BoardPostsEntity;
import com.kbs.www.entities.ReportsEntity;
import com.kbs.www.entities.UserEntity;
import com.kbs.www.services.AdminPageService;
import com.kbs.www.vos.BoardPostPageVo;
import com.kbs.www.vos.ReportsPageVo;
import com.kbs.www.vos.UserPageVo;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/")
public class AdminPageController {

    private final AdminPageService adminPageService;

    @Autowired
    public AdminPageController(AdminPageService adminPageService) {
        this.adminPageService = adminPageService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getIndex() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/adminIndex");
        return modelAndView;
    }

    @RequestMapping(value = "write/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getWrite() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/adminWrite");
        return modelAndView;
    }

    @RequestMapping(value = "write/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> postWrite(@RequestParam("title") String title,
                                       @RequestParam("location") String location,
                                       @RequestParam("startDate") String startDate,
                                       @RequestParam("endDate") String endDate,
                                       @RequestParam("description") String description,
                                       @RequestParam("coverData") MultipartFile coverFile) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);

        WriteEntity adminPage = new WriteEntity();
        adminPage.setTitle(title);
        adminPage.setLocation(location);
        adminPage.setStartDate(startDateTime);
        adminPage.setEndDate(endDateTime);
        adminPage.setDescription(description);

        Boolean result = this.adminPageService.write(adminPage, coverFile);
        System.out.println(result);
        Map<String, String> response = new HashMap<>();
        if (result) {
            response.put("result", result.toString());
            return ResponseEntity.ok(response);
        } else {
            response.put("result", result.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @RequestMapping(value = "user/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getUser(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                @RequestParam(value = "filter", required = false) String filter,
                                @RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView modelAndView = new ModelAndView();
        if (keyword == null) {
            Pair<UserPageVo, UserEntity[]> pair = this.adminPageService.selectUserPage(page);
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("user", pair.getRight());
        } else {
            Pair<UserPageVo, UserEntity[]> pair = this.adminPageService.selectUserPageBySearch(filter, keyword, page);
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("user", pair.getRight());
            modelAndView.addObject("filter", filter);
            modelAndView.addObject("keyword", keyword);
        }
        modelAndView.setViewName("admin/adminUser");
        return modelAndView;
    }

    @RequestMapping(value = "delete/", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchDelete(@RequestParam(value = "userEmail", required = false) String userEmail, @RequestParam(value = "index", required = false) Integer index) {
        JSONObject response = new JSONObject();
        if (userEmail != null) {
            Boolean result = this.adminPageService.updateDeleted(userEmail);
            response.put("result", result);
        }
        if (index != null) {
            Boolean result = this.adminPageService.deleteBoardPost(index);
            response.put("result", result);
        }
        return response.toString();
    }

    @RequestMapping(value = "warning/", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchWarning(@RequestParam(value = "userEmail", required = false) String userEmail, @RequestParam(value = "warning", required = false, defaultValue = "0") int warning) {
        Boolean result = this.adminPageService.updateWarning(userEmail, warning);
        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }

    @RequestMapping(value = "board/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getBoard(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                 @RequestParam(value = "filter", required = false) String filter,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView modelAndView = new ModelAndView();
        if (keyword == null) {
            Pair<BoardPostPageVo, BoardPostsEntity[]> pair = this.adminPageService.selectBoardPost(page);
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("board", pair.getRight());
        } else {
            Pair<BoardPostPageVo, BoardPostsEntity[]> pair = this.adminPageService.selectBoardPostBySearch(filter, keyword, page);
            modelAndView.addObject("page", pair.getLeft());
            modelAndView.addObject("board", pair.getRight());
            modelAndView.addObject("filter", filter);
            modelAndView.addObject("keyword", keyword);
        }
        modelAndView.setViewName("admin/adminBoard");
        return modelAndView;
    }

    @RequestMapping(value = "reports/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getReports(@RequestParam(value = "userEmail", required = false) String userEmail,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<ReportsPageVo, ReportsEntity[]> pair = this.adminPageService.selectReports(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("reports", pair.getRight());
        modelAndView.setViewName("admin/adminReports");
        return modelAndView;
    }

    @RequestMapping(value = "reports/index", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postReports(@RequestParam(value = "index", required = false) Integer index) {
        Boolean result = this.adminPageService.updateReport(index);
        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }
}
