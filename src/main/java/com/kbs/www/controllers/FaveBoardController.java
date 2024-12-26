package com.kbs.www.controllers;

import com.kbs.www.entities.FaveInfoEntity;
import com.kbs.www.services.FaveService;
import com.kbs.www.vos.FaveBoardVo;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.Param;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
@RequestMapping(value = "/fave")
public class FaveBoardController {

    private final FaveService faveService;

    @Autowired
    public FaveBoardController(FaveService faveService) {
        this.faveService = faveService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView getBoard(@RequestParam(value = "page", required = false, defaultValue = "1")int page) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<FaveBoardVo, FaveInfoEntity[]> pair = this.faveService.selectFaveInfo(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("fave", pair.getRight());
        modelAndView.setViewName("board/faveBoard");
        return modelAndView;
    }

    @RequestMapping(value = "read/", method = RequestMethod.GET)
    public ModelAndView getReadBoard(@RequestParam(value = "index") int index) {
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);
        this.faveService.updateFaveInfo(fave);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("fave", fave);
        modelAndView.setViewName("board/faveRead");
        return modelAndView;
    }

    @RequestMapping(value = "image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);
        if (fave == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentLength(fave.getCoverData().length)
                .contentType(MediaType.parseMediaType(fave.getCoverContentType()))
                .body(fave.getCoverData());
    }

    @RequestMapping(value = "/get-address", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> getAddress(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);
        if (fave == null || fave.getLocation() == null) {
            return ResponseEntity.notFound().build();
        }
        JSONObject response = new JSONObject();
        response.put("address", fave.getLocation());
        return ResponseEntity.ok(response.toString());
    }
}
