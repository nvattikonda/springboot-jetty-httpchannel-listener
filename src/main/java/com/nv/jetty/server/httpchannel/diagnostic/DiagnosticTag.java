package com.nv.jetty.server.httpchannel.diagnostic;

public enum DiagnosticTag {
    RequestBegin, RequestContent, RequestContentEnd, RequestEnd, ResponseBegin, ResponseCommit, ResponseContent, ResponseEnd, BeforeDispatch;
}
