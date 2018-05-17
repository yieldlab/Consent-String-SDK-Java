package com.yieldlab.gdpr.util;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.yieldlab.gdpr.GdprConsent;

public class GdprSwitchTest {

    @Test
    public void isSwitchedOnWhenInstantInPast() {
        // given
        GdprSwitch underTest = GdprSwitch
            .fromInstant(ISO_OFFSET_DATE_TIME.parse("2017-05-25T00:00:00+02:00", Instant::from));

        // then
        assertThat(underTest.isOn(), Matchers.is(true));
    }

    // 2018-05-25T00:00:00+02:00

    @Test
    public void isSwitchedOffWhenInstantInFuture() {
        // given
        GdprSwitch underTest = GdprSwitch
            .fromInstant(ISO_OFFSET_DATE_TIME.parse("2078-05-25T00:00:00+02:00", Instant::from));

        // then
        assertThat(underTest.isOn(), Matchers.is(false));
    }

    @Test
    public void isVendorAllowedWhenSwitchIsOff() {
        // given
        String consentString = "BN5lERiOMYEdiAKAWXEND1HoSBE6CAFAApAMgBkIDIgM0AgOJxAnQA==";
        GdprConsent consent = GdprConsent.fromBase64String(consentString);
        GdprSwitch underTest = GdprSwitch
            .fromInstant(ISO_OFFSET_DATE_TIME.parse("2078-05-25T00:00:00+02:00", Instant::from));

        // then
        assertTrue(consent.isVendorAllowed(225));
        assertThat(underTest.isOn(), Matchers.is(false));
        assertThat(underTest.isVendorAllowed(consent, 225), Matchers.is(true));
        assertThat(underTest.isVendorAllowed(consent, 411), Matchers.is(true));
    }

    @Test
    public void isVendorAllowedWhenSwitchIsOn() {
        // given
        String consentString = "BN5lERiOMYEdiAKAWXEND1HoSBE6CAFAApAMgBkIDIgM0AgOJxAnQA==";
        GdprConsent consent = GdprConsent.fromBase64String(consentString);
        GdprSwitch underTest = GdprSwitch
            .fromInstant(ISO_OFFSET_DATE_TIME.parse("2017-05-25T00:00:00+02:00", Instant::from));

        // then
        assertTrue(consent.isVendorAllowed(225));
        assertThat(underTest.isOn(), Matchers.is(true));
        assertThat(underTest.isVendorAllowed(consent, 225), Matchers.is(true));
    }

    @Test
    public void isVendorNotAllowedWhenConsentNotProvided() {
        // given
        GdprSwitch underTest = GdprSwitch
            .fromInstant(ISO_OFFSET_DATE_TIME.parse("2017-05-25T00:00:00+02:00", Instant::from));

        // then
        assertThat(underTest.isOn(), Matchers.is(true));
        assertThat(underTest.isVendorAllowed(null, 225), Matchers.is(false));
    }

    @Test
    public void isVendorNotAllowedWhenConsentProvidedButVendorNotAllowed() {
        // given
        String consentString = "BN5lERiOMYEdiAKAWXEND1HoSBE6CAFAApAMgBkIDIgM0AgOJxAnQA==";
        GdprConsent consent = GdprConsent.fromBase64String(consentString);
        GdprSwitch underTest = GdprSwitch
            .fromInstant(ISO_OFFSET_DATE_TIME.parse("2017-05-25T00:00:00+02:00", Instant::from));

        // then
        assertFalse(consent.isVendorAllowed(411));
        assertThat(underTest.isOn(), Matchers.is(true));
        assertThat(underTest.isVendorAllowed(consent, 411), Matchers.is(false));
    }
}