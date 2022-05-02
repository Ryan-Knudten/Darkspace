package Networking;

/**
 * RequestType
 *
 * Enum with all available functions that the server can process
 *
 * @author Ryan Knudten, 22
 *
 * @version 5/1/22
 *
 */
public enum RequestType {
    REFRESH,
    CREATE_USER,
    LOGIN_USER,
    DELETE_USER,
    CREATE_COURSE,
    DELETE_COURSE,
    JOIN_COURSE,
    LEAVE_COURSE,
    CREATE_QUIZ,
    EDIT_QUIZ,
    DELETE_QUIZ,
    GRADE_QUIZ,
    TAKE_QUIZ,
}