package com.kbs.www.services;

import com.kbs.www.entities.FaveInfoEntity;
import com.kbs.www.entities.BoardPostsEntity;
import com.kbs.www.entities.ReportsEntity;
import com.kbs.www.entities.UserEntity;
import com.kbs.www.mappers.*;
import com.kbs.www.vos.BoardPostPageVo;
import com.kbs.www.vos.IndexPageVo;
import com.kbs.www.vos.ReportsPageVo;
import com.kbs.www.vos.UserPageVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminPageService {
    private final WriteMapper writeMapper;
    private final BoardPostsMapper boardPostsMapper;
    private final UserMapper userMapper;
    private final ReportsMapper reportsMapper;
    private final FaveInfoMapper faveInfoMapper;

    @Autowired
    public AdminPageService(WriteMapper writeMapper, BoardPostsMapper boardPostsMapper, UserMapper userMapper, ReportsMapper reportsMapper, FaveInfoMapper faveInfoMapper) {
        this.writeMapper = writeMapper;
        this.boardPostsMapper = boardPostsMapper;
        this.userMapper = userMapper;
        this.reportsMapper = reportsMapper;
        this.faveInfoMapper = faveInfoMapper;
    }

    public Pair<IndexPageVo, UserEntity[]> selectIndexUser(int page) {
        page = Math.max(page, 1);
        int totalCount = this.userMapper.selectUserCount();
        IndexPageVo index = new IndexPageVo(page, totalCount);
        UserEntity[] user = this.userMapper.selectUserPage(index.countPerPage, index.offsetCount);
        return Pair.of(index, user);
    }

    public Pair<IndexPageVo, BoardPostsEntity[]> selectIndexBoard(int page) {
        page = Math.max(page, 1);
        int totalCount = this.boardPostsMapper.selectBoardPostCount();
        IndexPageVo index = new IndexPageVo(page, totalCount);
        BoardPostsEntity[] boardPosts = this.boardPostsMapper.selectBoardPost(index.countPerPage, index.offsetCount);

        for (BoardPostsEntity boardPost : boardPosts) {
            UserEntity user = this.findUserByEmail(boardPost.getUserEmail());
            boardPost.setUser(user);
        }
        return Pair.of(index, boardPosts);
    }

    public Pair<IndexPageVo, ReportsEntity[]> selectIndexReport(int page) {
        page = Math.max(page, 1);
        int totalCount = this.reportsMapper.selectReportsCount();
        IndexPageVo index = new IndexPageVo(page, totalCount);
        ReportsEntity[] reports = this.reportsMapper.selectReports(index.countPerPage, index.offsetCount);
        return Pair.of(index, reports);
    }

    public Boolean write(FaveInfoEntity adminPage, MultipartFile coverFile) {
        if (adminPage == null || adminPage.getTitle() == null || adminPage.getTitle().length() < 2 || adminPage.getTitle().length() > 20 ||
                adminPage.getLocation() == null || adminPage.getStartDate() == null || adminPage.getEndDate() == null || adminPage.getDescription() == null || adminPage.getDescription().isEmpty() || adminPage.getDescription().length() > 10000) {
            return false;
        }

        try {
            if (coverFile != null && !coverFile.isEmpty()) {
                adminPage.setCoverData(coverFile.getBytes());
                adminPage.setCoverContentType(coverFile.getContentType());
            }
            adminPage.setCreatedAt(LocalDateTime.now());
            adminPage.setUpdatedAt(null);

            adminPage.setUserEmail("yellow6480@gmail.com");

            return this.writeMapper.insertAdminWrite(adminPage) > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean modify(FaveInfoEntity adminPage, MultipartFile coverFile, Boolean deleteCover) {
        if (adminPage == null) {
            return false;
        }

        FaveInfoEntity dbFaveInfo = this.faveInfoMapper.selectFaveInfoById(adminPage.getIndex());
        if (dbFaveInfo == null) {
            return false;
        }

        dbFaveInfo.setTitle(adminPage.getTitle());
        dbFaveInfo.setDescription(adminPage.getDescription());
        dbFaveInfo.setStartDate(adminPage.getStartDate());
        dbFaveInfo.setEndDate(adminPage.getEndDate());
        dbFaveInfo.setLocation(adminPage.getLocation());
        dbFaveInfo.setUpdatedAt(LocalDateTime.now());

        try {
            if (Boolean.TRUE.equals(deleteCover)) {
                // 이미지 삭제 플래그가 설정된 경우
                dbFaveInfo.setCoverData(null);
                dbFaveInfo.setCoverContentType(null);
            } else if (coverFile != null && !coverFile.isEmpty()) {
                // 새로운 이미지가 업로드된 경우
                dbFaveInfo.setCoverData(coverFile.getBytes());
                dbFaveInfo.setCoverContentType(coverFile.getContentType());
            }
            // 이미지 유지 시 아무 작업도 하지 않음
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return this.faveInfoMapper.updateFaveInfo(dbFaveInfo) > 0;
    }


    public boolean updateDeleted(String userEmail) {
        UserEntity user = this.userMapper.selectUserByEmailAdmin(userEmail);
        if (user == null) {
            return false;
        }
        user.setDeletedAt(LocalDateTime.now());
        user.setSuspended(true);
        return this.userMapper.updateWarning(user) > 0;
    }

    public boolean updateWarning(String userEmail, int warning) {
        UserEntity user = this.userMapper.selectUserByEmailAdmin(userEmail);
        if (user == null) {
            return false;
        }
        if (user.getWarning() < 3) {
            user.setWarning(warning + 1);
        }
        return this.userMapper.updateWarning(user) > 0;
    }

    public Pair<UserPageVo, UserEntity[]> selectUserPage(int page) {
        page = Math.max(page, 1);
        int totalCount = this.userMapper.selectUserCount();
        UserPageVo userPageVo = new UserPageVo(page, totalCount);
        UserEntity[] user = this.userMapper.selectUserPage(userPageVo.countPerPage, userPageVo.offsetCount);
        return Pair.of(userPageVo, user);
    }

    public Pair<UserPageVo, UserEntity[]> selectUserPageBySearch(String filter, String keyword, int page) {
        page = Math.max(page, 1);
        if (filter == null || (!filter.equals("email") && !filter.equals("nickname") && !filter.equals("contact") && !filter.equals("verified"))) {
            filter = "email";
        }
        if (keyword == null || keyword.isEmpty()) {
            keyword = "";
        }
        if (filter.equals("verified")) {
            if (keyword.equals("완료")) {
                keyword = "1";
            }
            if (keyword.equals("미완료")) {
                keyword = "";
            }
        }
        int totalCount = this.userMapper.selectUserCountBySearch(filter, keyword);
        UserPageVo userPageVo = new UserPageVo(page, totalCount);
        UserEntity[] user = this.userMapper.selectUserBySearch(filter, keyword, userPageVo.countPerPage, userPageVo.offsetCount);
        return Pair.of(userPageVo, user);
    }

    public Pair<BoardPostPageVo, BoardPostsEntity[]> selectBoardPost(int page) {
        page = Math.max(page, 1);
        int totalCount = this.boardPostsMapper.selectBoardPostCount();
        BoardPostPageVo boardPostPageVo = new BoardPostPageVo(page, totalCount);
        BoardPostsEntity[] boardPosts = this.boardPostsMapper.selectBoardPost(boardPostPageVo.countPerPage, boardPostPageVo.offsetCount);

        for (BoardPostsEntity boardPost : boardPosts) {
            UserEntity user = this.findUserByEmail(boardPost.getUserEmail());
            boardPost.setUser(user);
        }
        return Pair.of(boardPostPageVo, boardPosts);
    }

    public Pair<BoardPostPageVo, BoardPostsEntity[]> selectBoardPostBySearch(String filter, String keyword, int page) {
        page = Math.max(page, 1);
        if (filter == null || (!filter.equals("all") && !filter.equals("title") && !filter.equals("content") && !filter.equals("nickname"))) {
            filter = "all";
        }
        if (keyword == null || keyword.isEmpty()) {
            keyword = "";
        }
        int totalCount = this.boardPostsMapper.selectBoardPostCountBySearch(filter, keyword);
        BoardPostPageVo boardPostPageVo = new BoardPostPageVo(page, totalCount);
        BoardPostsEntity[] boardPosts = this.boardPostsMapper.selectBoardPostBySearch(filter, keyword, boardPostPageVo.countPerPage, boardPostPageVo.offsetCount);

        for (BoardPostsEntity boardPost : boardPosts) {
            UserEntity user = this.findUserByEmail(boardPost.getUserEmail());
            boardPost.setUser(user);
        }
        return Pair.of(boardPostPageVo, boardPosts);
    }

    public UserEntity findUserByEmail(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            return null;
        }
        return this.userMapper.selectUserByEmailAdmin(userEmail);

    }

    public boolean deleteBoardPost(int index) {
        BoardPostsEntity board = this.boardPostsMapper.selectBoardPostsByIndex(index);
        if (board == null) {
             return false;
        }
        board.setDeletedAt(LocalDateTime.now());
        return this.boardPostsMapper.updateBoardPost(board) > 0;
    }

    public Pair<ReportsPageVo, ReportsEntity[]> selectReports(int page) {
        page = Math.max(page, 1);

        int totalCount = this.reportsMapper.selectReportsCount();
        ReportsPageVo reportsPageVo = new ReportsPageVo(page, totalCount);
        ReportsEntity[] reports = this.reportsMapper.selectReports(reportsPageVo.countPerPage, reportsPageVo.offsetCount);
        for (ReportsEntity report : reports) {
            UserEntity user = this.findUserByEmail(report.getReportUserEmail());
            report.setUser(user);
        }
        return Pair.of(reportsPageVo, reports);
    }

    public Boolean updateReport(int index) {
        ReportsEntity reports = this.reportsMapper.selectReportByIndex(index);
        if (reports == null) {
            return false;
        }
        reports.setCurrentStatus("신고 처리 완료");
        return this.reportsMapper.updateReport(reports) > 0;
    }

    public Pair<UserPageVo, FaveInfoEntity[]> selectFaveInfo(int page) {
        page = Math.max(page, 1);
        int totalCount = this.faveInfoMapper.selectFaveInfoCount();
        UserPageVo userPageVo = new UserPageVo(page, totalCount);
        FaveInfoEntity[] fave = this.faveInfoMapper.selectFaveInfo(userPageVo.countPerPage, userPageVo.offsetCount);
        return Pair.of(userPageVo, fave);
    }

    public Map<String, String> splitAddress(String fullAddress) {
        Map<String, String> addressParts = new HashMap<>();

        String extraAddress = "";
        if (fullAddress.contains("(") && fullAddress.contains(")")) {
            int startIdx = fullAddress.indexOf("(");
            int endIdx = fullAddress.indexOf(")");
            extraAddress = fullAddress.substring(startIdx, endIdx + 1);
            fullAddress = fullAddress.substring(0, startIdx).trim();
        }

        int lastSpaceIdx = fullAddress.lastIndexOf(" ");
        String mainAddress = fullAddress.substring(0, lastSpaceIdx).trim();
        String detailAddress = fullAddress.substring(lastSpaceIdx + 1).trim();

        addressParts.put("mainAddress", mainAddress);
        addressParts.put("detailAddress", detailAddress);
        addressParts.put("extraAddress", extraAddress);

        return addressParts;
    }

}