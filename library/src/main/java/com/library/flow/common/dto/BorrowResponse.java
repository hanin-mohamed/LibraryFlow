package com.library.flow.common.dto;


import java.time.Instant;
public record BorrowResponse(Instant returnedAt){}