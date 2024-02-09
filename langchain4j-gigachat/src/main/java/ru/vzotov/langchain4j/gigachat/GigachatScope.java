package ru.vzotov.langchain4j.gigachat;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum GigachatScope {
    @SerializedName("GIGACHAT_API_PERS") GIGACHAT_API_PERS,
    @SerializedName("GIGACHAT_API_CORP") GIGACHAT_API_CORP;
}
