package com.auruspay.logservice.exception;

@FunctionalInterface
public interface NoDataFoundExceptionFactory {

    NoDataFoundException create(String lookupKey);
}