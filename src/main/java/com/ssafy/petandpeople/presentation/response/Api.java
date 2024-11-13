package com.ssafy.petandpeople.presentation.response;

import com.ssafy.petandpeople.common.error.ErrorCodeIfs;

public class Api<T> {

    private Result result;
    private T body;

    public static Api<Object> OK() {
        Api<Object> api = new Api<>();
        api.result = Result.OK();
        return api;
    }

    public static <T> Api<T> OK(T body) {
        Api<T> api = new Api<>();
        api.result = Result.OK();
        api.body = body;
        return api;
    }

    public static Api<Object> ERROR(ErrorCodeIfs errorCodeIfs) {
        Api<Object> api = new Api<>();
        api.result = Result.ERROR(errorCodeIfs);
        return api;
    }

    public static Api<Object> ERROR(Integer errorCode) {
        Api<Object> api = new Api<>();
        api.result = Result.ERROR(errorCode);
        return api;
    }

    public Result getResult() {
        return result;
    }

    public T getBody() {
        return body;
    }

}