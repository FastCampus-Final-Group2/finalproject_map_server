package com.team2.finalprojectmapserver.exceptionhandler;

public record ErrorResponse (
    String statusMessage,
    String message
){

}