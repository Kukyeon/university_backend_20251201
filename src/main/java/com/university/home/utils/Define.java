package com.university.home.utils;

public class Define {

    // 세션 키
    public static final String PRINCIPAL = "principal";

    // 공통 메시지
    public static final String CREATE_FAIL = "생성에 실패하였습니다.";
    public static final String UPDATE_FAIL = "수정에 실패하였습니다.";
    public static final String NOT_FOUND_ID = "아이디를 찾을 수 없습니다.";
    public static final String WRONG_PASSWORD = "비밀번호가 틀렸습니다.";

    // 현재 학기/학년
    public static final int CURRENT_YEAR = 2025;
    public static final int CURRENT_SEMESTER = 1;

    // 파일 업로드 관련
    public static final String UPLOAD_DIRECTORY = "C:\\spring_upload\\universityManagement\\upload";
    public static final int MAX_FILE_SIZE = 1024 * 1024 * 20; // 20MB

    // 로그인 후 접근 가능한 페이지 목록
    public static final String[] PATHS = { "/update", "/password", "/info/**", "/guide", "/notice/**" };
    public static final String[] PROFESSOR_PATHS = { "/professor/**" };
    public static final String[] STUDENT_PATHS = { "/grade/**" };
    public static final String[] STAFF_PATHS = { "/user/**" };

    // 수강 가능한 최대 학점
    public static final int MAX_GRADES = 18;
}
