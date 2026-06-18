package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import org.apache.commons.lang3.RandomStringUtils;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Configuration extends BaseEntity {

    public static Configuration _blank() {
        return new Configuration();
    }

    private String sesUsername;
    private String sesPassword;
    private String sesLandlordCode;

    private boolean sesCredentialsValid = false;
    private boolean digitalSignatureEnabled = false;
    private boolean manualReviewEnabled = false;

    // Auto-configured variables
    @Setter(AccessLevel.NONE)
    private String rememberMeKey = RandomStringUtils.secureStrong().nextAlphanumeric(64);

    public boolean isSesConfigured() {
        return sesUsername != null && sesPassword != null && sesLandlordCode != null;
    }
}
