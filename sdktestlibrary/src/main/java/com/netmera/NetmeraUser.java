package com.netmera;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import com.netmera.internal.Optional;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

public class NetmeraUser {
    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;
    public static final int GENDER_NOT_SPECIFIED = 2;
    public static final int MARITAL_STATUS_SINGLE = 0;
    public static final int MARITAL_STATUS_MARRIED = 1;
    public static final int MARITAL_STATUS_NOT_SPECIFIED = 2;
    private transient Optional<String> userId;
    private transient Optional<String> email;
    private transient Optional<String> msisdn;
    @SerializedName("pa")
    private Optional<String> name;
    @SerializedName("pb")
    private Optional<String> surname;
    @SerializedName("pc")
    private Optional<List<String>> externalSegments;
    @SerializedName("pd")
    private Optional<Integer> gender;
    @SerializedName("pe")
    private Optional<Date> dateOfBirth;
    @SerializedName("pf")
    private Optional<Integer> maritalStatus;
    @SerializedName("pg")
    private Optional<Integer> childCount;
    @SerializedName("ph")
    private Optional<String> country;
    @SerializedName("pi")
    private Optional<String> state;
    @SerializedName("pj")
    private Optional<String> city;
    @SerializedName("pk")
    private Optional<String> district;
    @SerializedName("pl")
    private Optional<String> occupation;
    @SerializedName("pm")
    private Optional<String> industry;
    @SerializedName("pn")
    private Optional<String> favoriteTeam;
    @SerializedName("po")
    private Optional<String> language;

    public NetmeraUser() {
    }

    Optional<String> getUserId() {
        return this.userId;
    }

    Optional<String> getEmail() {
        return this.email;
    }

    Optional<String> getMsisdn() {
        return this.msisdn;
    }

    public void setUserId(@Nullable String userId) {
        this.userId = Optional.fromNullable(userId);
    }

    public void setEmail(@Nullable String email) {
        this.email = Optional.fromNullable(email);
    }

    public void setMsisdn(@Nullable String msisdn) {
        this.msisdn = Optional.fromNullable(msisdn);
    }

    public void setName(@Nullable String name) {
        this.name = Optional.fromNullable(name);
    }

    public void setSurname(@Nullable String surname) {
        this.surname = Optional.fromNullable(surname);
    }

    public void setExternalSegments(@Nullable List<String> externalSegments) {
        this.externalSegments = Optional.fromNullable(externalSegments);
    }

    public void setGender(@Nullable Integer gender) {
        this.gender = Optional.fromNullable(gender);
    }

    public void setDateOfBirth(@Nullable Date dateOfBirth) {
        if (dateOfBirth == null) {
            this.dateOfBirth = Optional.absent();
        } else {
            this.dateOfBirth = Optional.fromNullable(dateOfBirth);
        }

    }

    public void setMaritalStatus(@Nullable Integer maritalStatus) {
        this.maritalStatus = Optional.fromNullable(maritalStatus);
    }

    public void setChildCount(@Nullable Integer childCount) {
        this.childCount = Optional.fromNullable(childCount);
    }

    public void setCountry(@Nullable String country) {
        this.country = Optional.fromNullable(country);
    }

    public void setState(@Nullable String state) {
        this.state = Optional.fromNullable(state);
    }

    public void setCity(@Nullable String city) {
        this.city = Optional.fromNullable(city);
    }

    public void setDistrict(@Nullable String district) {
        this.district = Optional.fromNullable(district);
    }

    public void setOccupation(@Nullable String occupation) {
        this.occupation = Optional.fromNullable(occupation);
    }

    public void setIndustry(@Nullable String industry) {
        this.industry = Optional.fromNullable(industry);
    }

    public void setFavoriteTeam(@Nullable String favoriteTeam) {
        this.favoriteTeam = Optional.fromNullable(favoriteTeam);
    }

    public void setLanguage(@Nullable String language) {
        this.language = Optional.fromNullable(language);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MaritalStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Gender {
    }
}
