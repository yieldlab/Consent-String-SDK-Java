package com.iab.gdpr;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Objects;

import com.iab.gdpr.exception.GdprException;
import com.iab.gdpr.exception.VendorConsentCreateException;
import com.iab.gdpr.exception.VendorConsentException;
import com.iab.gdpr.exception.VendorConsentParseException;

/**
 * Copied from https://github.com/InteractiveAdvertisingBureau/Consent-String-SDK-Java and modified
 *
 * This class implements a parser for the IAB consent string as specified in
 * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/
 * Draft_for_Public_Comment_Transparency%20%26%20Consent%20Framework%20-%20cookie%20and%20vendor%20list%20format%
 * 20specification%20v1.0a.pdf
 *
 */
public class GdprConsent {

    private static final int VENDOR_ENCODING_RANGE = 1;

    private static final int VERSION_BIT_OFFSET = 0;
    private static final int VERSION_BIT_SIZE = 6;
    private static final int CREATED_BIT_OFFSET = 6;
    private static final int CREATED_BIT_SIZE = 36;
    private static final int UPDATED_BIT_OFFSET = 42;
    private static final int UPDATED_BIT_SIZE = 36;
    private static final int CMP_ID_OFFSET = 78;
    private static final int CMP_ID_SIZE = 12;
    private static final int CMP_VERSION_OFFSET = 90;
    private static final int CMP_VERSION_SIZE = 12;
    private static final int CONSENT_SCREEN_SIZE_OFFSET = 102;
    private static final int CONSENT_SCREEN_SIZE = 6;
    private static final int CONSENT_LANGUAGE_OFFSET = 108;
    private static final int CONSENT_LANGUAGE_SIZE = 12;
    private static final int VENDOR_LIST_VERSION_OFFSET = 120;
    private static final int VENDOR_LIST_VERSION_SIZE = 12;
    private static final int PURPOSES_OFFSET = 132;
    private static final int PURPOSES_SIZE = 24;
    private static final int MAX_VENDOR_ID_OFFSET = 156;
    private static final int MAX_VENDOR_ID_SIZE = 16;
    private static final int ENCODING_TYPE_OFFSET = 172;
    private static final int ENCODING_TYPE_SIZE = 1;
    private static final int VENDOR_BITFIELD_OFFSET = 173;
    private static final int DEFAULT_CONSENT_OFFSET = 173;
    private static final int NUM_ENTRIES_OFFSET = 174;
    private static final int NUM_ENTRIES_SIZE = 12;
    private static final int RANGE_ENTRY_OFFSET = 186;
    private static final int VENDOR_ID_SIZE = 16;
    private static Decoder decoder = Base64.getUrlDecoder();
    private static Encoder encoder = Base64.getUrlEncoder();
    private final Bits bits;
    // fields contained in the consent string
    private final int version;
    private final Instant consentRecordCreated;
    private final Instant consentRecordLastUpdated;
    private final int cmpID;
    private final int cmpVersion;
    private final int consentScreenID;
    private final String consentLanguage;
    private final int vendorListVersion;
    private final int maxVendorSize;
    private final int vendorEncodingType;
    private final List<Boolean> allowedPurposes = new ArrayList<>();
    // only used when range entry is enabled
    private boolean defaultConsent;
    private List<RangeEntry> rangeEntries;
    private String consentString;
    private List<Integer> integerPurposes = null;

    /**
     * Constructor.
     *
     * @param consentString
     *            (required). The binary user consent data encoded as url and filename safe base64 string
     *
     * @throws VendorConsentException
     *             if the consent string cannot be parsed
     */
    private GdprConsent(String consentString) throws VendorConsentException {
        this(decoder.decode(consentString));
        this.consentString = consentString;
    }

    /**
     * Constructor
     *
     * @param bytes:
     *            the byte string encoding the user consent data
     * @throws VendorConsentException
     *             when the consent string cannot be parsed
     */
    private GdprConsent(byte[] bytes) throws VendorConsentException {
        this.bits = new Bits(bytes);
        // begin parsing

        this.version = bits.getInt(VERSION_BIT_OFFSET, VERSION_BIT_SIZE);
        this.consentRecordCreated = bits.getInstantFromEpochDeciseconds(CREATED_BIT_OFFSET, CREATED_BIT_SIZE);
        this.consentRecordLastUpdated = bits.getInstantFromEpochDeciseconds(UPDATED_BIT_OFFSET, UPDATED_BIT_SIZE);
        this.cmpID = bits.getInt(CMP_ID_OFFSET, CMP_ID_SIZE);
        this.cmpVersion = bits.getInt(CMP_VERSION_OFFSET, CMP_VERSION_SIZE);
        this.consentScreenID = bits.getInt(CONSENT_SCREEN_SIZE_OFFSET, CONSENT_SCREEN_SIZE);
        this.consentLanguage = bits.getSixBitString(CONSENT_LANGUAGE_OFFSET, CONSENT_LANGUAGE_SIZE);
        this.vendorListVersion = bits.getInt(VENDOR_LIST_VERSION_OFFSET, VENDOR_LIST_VERSION_SIZE);
        this.maxVendorSize = bits.getInt(MAX_VENDOR_ID_OFFSET, MAX_VENDOR_ID_SIZE);
        this.vendorEncodingType = bits.getInt(ENCODING_TYPE_OFFSET, ENCODING_TYPE_SIZE);
        for (int i = PURPOSES_OFFSET, ii = PURPOSES_OFFSET + PURPOSES_SIZE; i < ii; i++) {
            allowedPurposes.add(bits.getBit(i));
        }
        if (vendorEncodingType == VENDOR_ENCODING_RANGE) {
            this.rangeEntries = new ArrayList<>();
            this.defaultConsent = bits.getBit(DEFAULT_CONSENT_OFFSET);
            int numEntries = bits.getInt(NUM_ENTRIES_OFFSET, NUM_ENTRIES_SIZE);
            for (int i = 0, currentOffset = RANGE_ENTRY_OFFSET + 1; i < numEntries; i++, currentOffset++) {
                boolean range = bits.getBit(currentOffset - 1);
                if (range) {
                    int startVendorId = bits.getInt(currentOffset, VENDOR_ID_SIZE);
                    currentOffset += VENDOR_ID_SIZE;
                    int endVendorId = bits.getInt(currentOffset, VENDOR_ID_SIZE);
                    currentOffset += VENDOR_ID_SIZE;
                    rangeEntries.add(new RangeEntry(startVendorId, endVendorId));
                } else {
                    int vendorId = bits.getInt(currentOffset, VENDOR_ID_SIZE);
                    currentOffset += VENDOR_ID_SIZE;
                    rangeEntries.add(new RangeEntry(vendorId));
                }
            }
        }
    }

    public GdprConsent(Builder builder) throws VendorConsentException {
        this.version = builder.version;
        this.consentRecordCreated = builder.consentRecordCreated;
        this.consentRecordLastUpdated = builder.consentRecordLastUpdated;
        this.cmpID = builder.cmpID;
        this.cmpVersion = builder.cmpVersion;
        this.consentScreenID = builder.consentScreenID;
        this.consentLanguage = builder.consentLanguage;
        this.vendorListVersion = builder.vendorListVersion;
        this.maxVendorSize = builder.maxVendorSize;
        this.vendorEncodingType = builder.vendorEncodingType;
        this.allowedPurposes.addAll(builder.allowedPurposes);
        this.defaultConsent = builder.defaultConsent;
        this.rangeEntries = builder.rangeEntries;
        this.integerPurposes = builder.integerPurposes;

        int rangeEntrySize = 0;
        for (RangeEntry entry : rangeEntries) {
            if (entry.maxVendorId == entry.minVendorId) {
                rangeEntrySize += VENDOR_ID_SIZE;
            } else {
                rangeEntrySize += VENDOR_ID_SIZE * 2;
            }
        }
        this.bits = new Bits(new byte[(RANGE_ENTRY_OFFSET + rangeEntrySize) / 8 + 1]);

        bits.setInt(VERSION_BIT_OFFSET, VERSION_BIT_SIZE, this.version);
        bits.setInstantToEpochDeciseconds(CREATED_BIT_OFFSET, CREATED_BIT_SIZE, this.consentRecordCreated);
        bits.setInstantToEpochDeciseconds(UPDATED_BIT_OFFSET, UPDATED_BIT_SIZE, this.consentRecordLastUpdated);

        bits.setInt(CMP_ID_OFFSET, CMP_ID_SIZE, this.cmpID);
        bits.setInt(CMP_VERSION_OFFSET, CMP_VERSION_SIZE, this.cmpVersion);
        bits.setInt(CONSENT_SCREEN_SIZE_OFFSET, CONSENT_SCREEN_SIZE, this.consentScreenID);
        bits.setSixBitString(CONSENT_LANGUAGE_OFFSET, CONSENT_LANGUAGE_SIZE, this.consentLanguage);

        bits.setInt(VENDOR_LIST_VERSION_OFFSET, VENDOR_LIST_VERSION_SIZE, this.vendorListVersion);

        int i = 0;
        for (boolean purpose : allowedPurposes) {
            if (purpose) {
                bits.setBit(PURPOSES_OFFSET + i++);
            } else {
                bits.unsetBit(PURPOSES_OFFSET + i++);
            }
        }

        bits.setInt(MAX_VENDOR_ID_OFFSET, MAX_VENDOR_ID_SIZE, this.maxVendorSize);
        bits.setInt(ENCODING_TYPE_OFFSET, ENCODING_TYPE_SIZE, this.vendorEncodingType);

        if (vendorEncodingType == VENDOR_ENCODING_RANGE) {
            if (defaultConsent) {
                bits.setBit(DEFAULT_CONSENT_OFFSET);
            } else {
                bits.unsetBit(DEFAULT_CONSENT_OFFSET);
            }
            bits.setInt(NUM_ENTRIES_OFFSET, NUM_ENTRIES_SIZE, rangeEntries.size());

            int currentOffset = RANGE_ENTRY_OFFSET;

            for (RangeEntry entry : rangeEntries) {
                if (entry.maxVendorId > entry.minVendorId) { // range
                    bits.setBit(currentOffset++);
                    bits.setInt(currentOffset, VENDOR_ID_SIZE, entry.minVendorId);
                    currentOffset += VENDOR_ID_SIZE;
                    bits.setInt(currentOffset, VENDOR_ID_SIZE, entry.maxVendorId);
                    currentOffset += VENDOR_ID_SIZE;
                } else {
                    bits.unsetBit(currentOffset++);
                    bits.setInt(currentOffset, VENDOR_ID_SIZE, entry.minVendorId);
                    currentOffset += VENDOR_ID_SIZE;
                }
            }
        }
        this.consentString = encoder.encodeToString(bits.toByteArray());
    }

    public static GdprConsent fromBase64String(String consentString) throws GdprException {
        try {
            return isNullOrEmpty(consentString) ? null : new GdprConsent(consentString);
        } catch (VendorConsentException e) {
            throw new GdprException("Error parsing IAB Consent String", e.getCause());
        }
    }

    /**
     * @return the string passes in the constructor.
     *
     */
    public String getConsentString() {
        return consentString;
    }

    /**
     * @return the {@link Instant} at which the consent record was created
     */
    public Instant getConsentRecordCreated() {
        return consentRecordCreated;
    }

    /**
     *
     * @return the {@link Instant} at which the cookie was last updated
     */
    public Instant getConsentRecordLastUpdated() {
        return consentRecordLastUpdated;
    }

    /**
     *
     * @return the version of the cookie format used in this consent string
     */
    public int getVersion() {
        return version;
    }

    /**
     *
     * @return the id of the consent management partner that created this consent string
     */
    public int getCmpId() {
        return cmpID;
    }

    /**
     * @return the version of the cmp being used
     */
    public int getCmpVersion() {
        return cmpVersion;
    }

    /**
     *
     * @return the id of the string through which the user gave consent in the CMP UI
     */
    public int getConsentScreen() {
        return consentScreenID;
    }

    /**
     * @return The two letter ISO639-1 language code in which the CMP asked for consent
     */
    public String getConsentLanguage() {
        return consentLanguage;
    }

    public int getMaxVendorSize() {
        return maxVendorSize;
    }

    public boolean isDefaultConsent() {
        return defaultConsent;
    }

    public int getVendorEncodingType() {
        return vendorEncodingType;
    }

    public List<RangeEntry> getRangeEntries() {
        return rangeEntries;
    }

    public String getBinaryString() {
        return bits.getBinaryString();
    }

    /**
     *
     * @return a list of purpose id's which are permitted according to this consent string
     */
    public List<Integer> getAllowedPurposes() {
        if (integerPurposes != null) {
            return integerPurposes;
        }
        List<Integer> purposes = new ArrayList<>();
        for (int i = 1, ii = allowedPurposes.size(); i <= ii; i++) {
            if (isPurposeAllowed(i)) {
                purposes.add(i);
            }
        }
        integerPurposes = purposes;
        return purposes;

    }

    /**
     *
     * @return the vendor list version which was used in creating this consent string
     */
    public int getVendorListVersion() {
        return vendorListVersion;
    }

    /**
     * @return a boolean describing the user consent status for a particular purpose. The lowest purpose ID is 1.
     */
    public boolean isPurposeAllowed(int purposeId) {
        if (purposeId < 1 || purposeId > allowedPurposes.size()) {
            return false;
        }
        return allowedPurposes.get(purposeId - 1);
    }

    public boolean arePurposesAllowed(List<Integer> purposeIds) {
        boolean allow = true;
        for (int purposeId : purposeIds) {
            allow = allow && isPurposeAllowed(purposeId);
        }
        return allow;
    }

    private boolean findVendorIdInRange(int vendorId) {
        for (RangeEntry entry : rangeEntries) {
            if (entry.containsVendorId(vendorId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return a boolean describing if a vendor has consented to a particular vendor. The lowest vendor ID is 1.
     *
     *         This method, along with {@link #isPurposeAllowed(int)} fully describes the user consent for a particular
     *         action by a given vendor.
     */
    public boolean isVendorAllowed(int vendorId) {
        if (vendorEncodingType == VENDOR_ENCODING_RANGE) {
            boolean present = findVendorIdInRange(vendorId);
            return present != defaultConsent;
        } else {
            return bits.getBit(VENDOR_BITFIELD_OFFSET + vendorId - 1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GdprConsent consent = (GdprConsent) o;
        return version == consent.version && cmpID == consent.cmpID && cmpVersion == consent.cmpVersion
                && consentScreenID == consent.consentScreenID && vendorListVersion == consent.vendorListVersion
                && maxVendorSize == consent.maxVendorSize && vendorEncodingType == consent.vendorEncodingType
                && defaultConsent == consent.defaultConsent && Objects.equals(bits, consent.bits)
                && Objects.equals(consentRecordCreated, consent.consentRecordCreated)
                && Objects.equals(consentRecordLastUpdated, consent.consentRecordLastUpdated)
                && Objects.equals(consentLanguage, consent.consentLanguage)
                && Objects.equals(allowedPurposes, consent.allowedPurposes)
                && Objects.equals(consentString, consent.consentString)
                && Objects.equals(rangeEntries, consent.rangeEntries)
                && Objects.equals(integerPurposes, consent.integerPurposes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(bits, version, consentRecordCreated, consentRecordLastUpdated, cmpID, cmpVersion,
                consentScreenID, consentLanguage, vendorListVersion, maxVendorSize, vendorEncodingType, allowedPurposes,
                consentString, rangeEntries, defaultConsent, integerPurposes);
    }

    @Override
    public String toString() {
        return "GdprConsent{" + "bits=" + bits + ", version=" + version + ", consentRecordCreated="
                + consentRecordCreated + ", consentRecordLastUpdated=" + consentRecordLastUpdated + ", cmpID=" + cmpID
                + ", cmpVersion=" + cmpVersion + ", consentScreenID=" + consentScreenID + ", consentLanguage='"
                + consentLanguage + '\'' + ", vendorListVersion=" + vendorListVersion + ", maxVendorSize="
                + maxVendorSize + ", vendorEncodingType=" + vendorEncodingType + ", allowedPurposes=" + allowedPurposes
                + ", consentString='" + consentString + '\'' + ", rangeEntries=" + rangeEntries + ", defaultConsent="
                + defaultConsent + ", integerPurposes=" + integerPurposes + '}';
    }

    // static classes
    public static class RangeEntry {
        /**
         * This class corresponds to the RangeEntry field given in the consent string specification.
         */
        private final int maxVendorId;
        private final int minVendorId;

        public RangeEntry(int vendorId) {
            this.maxVendorId = this.minVendorId = vendorId;
        }

        public RangeEntry(int startId, int endId) {
            this.maxVendorId = endId;
            this.minVendorId = startId;
        }

        public boolean containsVendorId(int vendorId) {
            return vendorId >= minVendorId && vendorId <= maxVendorId;
        }

        public boolean idIsGreaterThanMax(int vendorId) {
            return vendorId > maxVendorId;
        }

        public boolean isIsLessThanMin(int vendorId) {
            return vendorId < minVendorId;
        }

        @Override
        public String toString() {
            return "RangeEntry{" + "maxVendorId=" + maxVendorId + ", minVendorId=" + minVendorId + '}';
        }
    }

    public static class Builder {
        private int version;
        private Instant consentRecordCreated;
        private Instant consentRecordLastUpdated;
        private int cmpID;
        private int cmpVersion;
        private int consentScreenID;
        private String consentLanguage;
        private int vendorListVersion;
        private int maxVendorSize;
        private int vendorEncodingType;
        private List<Boolean> allowedPurposes = new ArrayList<>(PURPOSES_SIZE);
        // only used when range entry is enabled
        private List<RangeEntry> rangeEntries;
        private boolean defaultConsent;
        private List<Integer> integerPurposes = null;

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder withConsentRecordCreatedOn(Instant consentRecordCreated) {
            this.consentRecordCreated = consentRecordCreated;
            return this;
        }

        public Builder withConsentRecordLastUpdatedOn(Instant consentRecordLastUpdated) {
            this.consentRecordLastUpdated = consentRecordLastUpdated;
            return this;
        }

        public Builder withCmpID(int cmpID) {
            this.cmpID = cmpID;
            return this;
        }

        public Builder withCmpVersion(int cmpVersion) {
            this.cmpVersion = cmpVersion;
            return this;
        }

        public Builder withConsentScreenID(int consentScreenID) {
            this.consentScreenID = consentScreenID;
            return this;
        }

        public Builder withConsentLanguage(String consentLanguage) {
            this.consentLanguage = consentLanguage;
            return this;
        }

        public Builder withVendorListVersion(int vendorListVersion) {
            this.vendorListVersion = vendorListVersion;
            return this;
        }

        public Builder withMaxVendorSize(int maxVendorSize) {
            this.maxVendorSize = maxVendorSize;
            return this;
        }

        public Builder withVendorEncodingType(int vendorEncodingType) {
            this.vendorEncodingType = vendorEncodingType;
            return this;
        }

        public Builder withAllowedPurposes(List<Integer> allowedPurposes) {
            this.integerPurposes = allowedPurposes;
            for (int i = 0; i < PURPOSES_SIZE; i++) {
                this.allowedPurposes.add(false);
            }
            for (int purpose : allowedPurposes) {
                this.allowedPurposes.set(purpose - 1, true);
            }
            return this;
        }

        public Builder withRangeEntries(List<RangeEntry> rangeEntries) {
            this.rangeEntries = rangeEntries;
            return this;
        }

        public Builder withDefaultConsent(boolean defaultConsent) {
            this.defaultConsent = defaultConsent;
            return this;
        }

        public GdprConsent build() {
            return new GdprConsent(this);
        }
    }

    // since java.util.BitSet is inappropiate to use here--as it reversed the bit order of the consent string--we
    // implement our own bitwise operations here.
    private static class Bits {
        // big endian
        private static final byte[] bytePows = { -128, 64, 32, 16, 8, 4, 2, 1 };
        private final byte[] bytes;

        public Bits(byte[] b) {
            this.bytes = b;
        }

        /**
         *
         * @param index:
         *            the nth number bit to get from the bit string
         * @return boolean bit, true if the bit is switched to 1, false otherwise
         */
        public boolean getBit(int index) {
            int byteIndex = index / 8;
            int bitExact = index % 8;
            byte b = bytes[byteIndex];
            return (b & bytePows[bitExact]) != 0;
        }

        /**
         *
         * @param index:
         *            set the nth number bit from the bit string
         */
        public void setBit(int index) {
            int byteIndex = index / 8;
            int shift = (byteIndex + 1) * 8 - index - 1;
            bytes[byteIndex] |= 1 << shift;
        }

        /**
         *
         * @param index:
         *            unset the nth number bit from the bit string
         */
        public void unsetBit(int index) {
            int byteIndex = index / 8;
            int shift = (byteIndex + 1) * 8 - index - 1;
            bytes[byteIndex] &= ~(1 << shift);
        }

        /**
         * interprets n number of bits as a big endiant int
         *
         * @param startInclusive:
         *            the nth to begin interpreting from
         * @param size:
         *            the number of bits to interpret
         * @return
         * @throws VendorConsentException
         *             when the bits cannot fit in an int sized field
         */
        public int getInt(int startInclusive, int size) throws VendorConsentException {
            if (size > Integer.SIZE) {
                throw new VendorConsentParseException("can't fit bit range in int " + size);
            }
            int val = 0;
            int sigMask = 1;
            int sigIndex = size - 1;

            for (int i = 0; i < size; i++) {
                if (getBit(startInclusive + i)) {
                    val += (sigMask << sigIndex);
                }
                sigIndex--;
            }
            return val;
        }

        /**
         * Writes an integer value into a bit array of given size
         *
         * @param startInclusive:
         *            the nth to begin writing to
         * @param size:
         *            the number of bits available to write
         * @param to:
         *            the integer to write out
         * @throws VendorConsentException
         *             when the bits cannot fit into the provided size
         */
        public void setInt(int startInclusive, int size, int to) throws VendorConsentException {
            if (size > Integer.SIZE || to > maxOfSize(size) || to < 0) {
                // TODO check againt the size
                throw new VendorConsentCreateException("can't fit integer into bit range of size" + size);
            }
            for (int i = size - 1; i >= 0; i--) {
                int index = startInclusive + i;
                int byteIndex = index / 8;
                int shift = (byteIndex + 1) * 8 - index - 1;
                bytes[byteIndex] |= (to % 2) << shift;
                to /= 2;
            }
        }

        /**
         * interprets n bits as a big endian long
         *
         * @param startInclusive:
         *            the nth to begin interpreting from
         * @param size:the
         *            number of bits to interpret
         * @return the long value create by interpretation of provided bits
         * @throws VendorConsentException
         *             when the bits cannot fit in an int sized field
         */
        public long getLong(int startInclusive, int size) throws VendorConsentException {
            if (size > Long.SIZE) {
                throw new VendorConsentParseException("can't fit bit range in long: " + size);
            }
            long val = 0;
            long sigMask = 1;
            int sigIndex = size - 1;

            for (int i = 0; i < size; i++) {
                if (getBit(startInclusive + i)) {
                    val += (sigMask << sigIndex);
                }
                sigIndex--;
            }
            return val;
        }

        /**
         * Writes a long value into a bit array of given size
         *
         * @param startInclusive:
         *            the nth to begin writing to
         * @param size:
         *            the number of bits available to write
         * @param to:
         *            the long number to write out
         * @throws VendorConsentException
         *             when the bits cannot fit into the provided size
         */
        public void setLong(int startInclusive, int size, long to) throws VendorConsentException {
            if (size > Long.SIZE || to > maxOfSize(size) || to < 0) {
                // TODO check againt the size
                throw new VendorConsentCreateException("can't fit long into bit range of size " + size);
            }

            for (int i = size - 1; i >= 0; i--) {
                int index = startInclusive + i;
                int byteIndex = index / 8;
                int shift = (byteIndex + 1) * 8 - index - 1;
                bytes[byteIndex] |= (to % 2) << shift;
                to /= 2;
            }
        }

        /**
         * returns an {@link Instant} derived from interpreting the given interval on the bit string as long
         * representing the number of demiseconds from the unix epoch
         *
         * @param startInclusive:
         *            the bit from which to begin interpreting
         * @param size:
         *            the number of bits to interpret
         * @return
         * @throws VendorConsentException
         *             when the number of bits requested cannot fit in a long
         */
        public Instant getInstantFromEpochDeciseconds(int startInclusive, int size) throws VendorConsentException {
            long epochDemi = getLong(startInclusive, size);
            return Instant.ofEpochMilli(epochDemi * 100);
        }

        public void setInstantToEpochDeciseconds(int startInclusive, int size, Instant instant)
                throws VendorConsentException {
            setLong(startInclusive, size, instant.toEpochMilli() / 100);
        }

        /**
         * @return the number of bits in the bit string
         *
         */
        public int length() {
            return bytes.length * 8;
        }

        /**
         * This method interprets the given interval in the bit string as a series of six bit characters, where 0=A and
         * 26=Z
         *
         * @param startInclusive:
         *            the nth bit in the bitstring from which to start the interpretation
         * @param size:
         *            the number of bits to include in the string
         * @return the string given by the above interpretation
         * @throws VendorConsentException
         *             when the requested interval is not a multiple of six
         */
        public String getSixBitString(int startInclusive, int size) throws VendorConsentException {
            if (size % 6 != 0) {
                throw new VendorConsentCreateException("string bit length must be multiple of six: " + size);
            }
            int charNum = size / 6;
            StringBuilder val = new StringBuilder();
            for (int i = 0; i < charNum; i++) {
                int charCode = getInt(startInclusive + (i * 6), 6) + 65;
                val.append((char) charCode);
            }
            return val.toString().toUpperCase();
        }

        /**
         * This method interprets characters, as 0=A and 26=Z and writes to the given interval in the bit string as a
         * series of six bits
         *
         * @param startInclusive:
         *            the nth bit in the bitstring from which to start writing
         * @param size:
         *            the size of the bitstring
         * @param to:
         *            the string given by the above interpretation
         * @throws VendorConsentException
         *             when the requested interval is not a multiple of six
         */
        public void setSixBitString(int startInclusive, int size, String to) throws VendorConsentException {
            if (size % 6 != 0 || size / 6 != to.length()) {
                throw new VendorConsentCreateException(
                        "bit array size must be multiple of six and equal to 6 times the size of string");
            }
            char[] values = to.toCharArray();
            for (int i = 0; i < values.length; i++) {
                int charCode = values[i] - 65;
                setInt(startInclusive + (i * 6), 6, charCode);
            }
        }

        /**
         *
         * @return a string representation of the byte array passed in the constructor. for example, a bit array of [4]
         *         yields a String of "0100"
         */
        public String getBinaryString() {
            StringBuilder s = new StringBuilder();
            int size = length();
            for (int i = 0; i < size; i++) {
                if (getBit(i)) {
                    s.append("1");
                } else {
                    s.append("0");
                }
            }
            return s.toString();
        }

        public byte[] toByteArray() {
            return bytes;
        }

        private long maxOfSize(int size) {
            long max = 0;
            for (int i = 0; i < size; i++) {
                max += Math.pow(2, i);
            }
            return max;
        }
    }
}