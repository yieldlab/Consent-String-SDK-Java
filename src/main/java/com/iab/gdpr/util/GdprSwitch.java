package com.iab.gdpr.util;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import java.time.Instant;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.iab.gdpr.GdprConsent;

public class GdprSwitch {

    // Hamburg (Germany - Hamburg) Friday, 25 May 2018, 00:00:00 CEST UTC+2 hours
    // Corresponding UTC (GMT) Thursday, 24 May 2018, 22:00:00
    private static Instant GDPR_SWITCH = ISO_OFFSET_DATE_TIME.parse("2018-05-25T00:00:00+02:00", Instant::from);

    private GdprSwitch(Instant gdprSwitch) {
        this.GDPR_SWITCH = gdprSwitch;
    }

    @VisibleForTesting
    static GdprSwitch fromInstant(Instant gdprSwitch) {
        return new GdprSwitch(gdprSwitch);
    }

    public static boolean isOn() {
        return Instant.now().isAfter(GDPR_SWITCH);
    }

    public static boolean isVendorAllowed(GdprConsent gdprConsent, int vendorId) {
        return !isOn()
                || Optional.ofNullable(gdprConsent).map(consent -> consent.isVendorAllowed(vendorId)).orElse(false);
    }
}
