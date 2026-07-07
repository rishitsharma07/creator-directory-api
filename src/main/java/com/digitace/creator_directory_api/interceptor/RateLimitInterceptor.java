package com.digitace.creator_directory_api.interceptor;

import com.digitace.creator_directory_api.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_WINDOW = 50;
    private static final long WINDOW_DURATION_MILLIS = 60_000;
    private static final int TOO_MANY_REQUESTS_STATUS = 429; // no SC_ constant exists for this in HttpServletResponse

    private final ConcurrentHashMap<UUID, RequestWindow> requestCounts =new ConcurrentHashMap<>();

    private static class RequestWindow{
        int count;
        long windowStartMillis;

        RequestWindow() {
            this.windowStartMillis = System.currentTimeMillis();
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler)throws Exception{

        UUID agencyId = TenantContext.getAgencyId();

        if (agencyId == null) {
            return true;
        }


        RequestWindow window = requestCounts.computeIfAbsent(agencyId, id -> new RequestWindow());

        synchronized (window){
            long elapsed = System.currentTimeMillis() - window.windowStartMillis;

            if (elapsed > WINDOW_DURATION_MILLIS){

                window.count=0;
                window.windowStartMillis = System.currentTimeMillis();
            }

            window.count++;

            if (window.count > MAX_REQUESTS_PER_WINDOW){
                response.sendError(TOO_MANY_REQUESTS_STATUS,"Rate limit exceeded. Try again after one minute.");
                return false;
            }
        }
        return true;
    }

}
