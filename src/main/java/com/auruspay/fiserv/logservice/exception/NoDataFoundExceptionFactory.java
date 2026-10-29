package com.auruspay.fiserv.logservice.exception;

@FunctionalInterface
public interface NoDataFoundExceptionFactory {

    NoDataFoundException create(String lookupKey);
}