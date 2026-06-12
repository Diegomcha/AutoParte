package me.diegomcha.autoparte.integration.ses;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SesAPI {

    private final WebServiceTemplate client;
}
