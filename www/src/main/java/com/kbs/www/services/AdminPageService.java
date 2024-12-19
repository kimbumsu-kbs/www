package com.kbs.www.services;

import com.kbs.www.entities.WriteEntity;
import com.kbs.www.entities.BoardPostsEntity;
import com.kbs.www.entities.ReportsEntity;
import com.kbs.www.entities.UserEntity;
import com.kbs.www.mappers.BoardPostsMapper;
import com.kbs.www.mappers.ReportsMapper;
import com.kbs.www.mappers.UserMapper;
import com.kbs.www.mappers.WriteMapper;
import com.kbs.www.vos.BoardPostPageVo;
import com.kbs.www.vos.ReportsPageVo;
import com.kbs.www.vos.UserPageVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class AdminPageService {
    private final WriteMapper writeMapper;
    private final BoardPostsMapper boardPostsMapper;
    private final UserMapper userMapper;
    private final ReportsMapper reportsMapper;

    @Autowired
    public AdminPageService(WriteMapper writeMapper, BoardPostsMapper boardPostsMapper, UserMapper userMapper, ReportsMapper reportsMapper) {
        this.writeMapper = writeMapper;
        this.boardPostsMapper = boardPostsMapper;
        this.userMapper = userMapper;
        this.reportsMapper = reportsMapper;
    }

    public Boolean write(WriteEntity adminPage, MultipartFile coverFile) {
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

    public UserEntity[] read(UserEntity user) {
        if (user == null) {
            return null;
        }
        return this.userMapper.selectAllUser();
    }

    public boolean updateDeleted(String userEmail) {
        UserEntity user = this.userMapper.selectUserByEmail(userEmail);
        if (user == null) {
            return false;
        }
        user.setDeletedAt(LocalDateTime.now());
        user.setSuspended(true);
        return this.userMapper.updateWarning(user) > 0;
    }

    public boolean updateWarning(String userEmail, int warning) {
        UserEntity user = this.userMapper.selectUserByEmail(userEmail);
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
        return this.userMapper.selectUserByEmail(userEmail);
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
}