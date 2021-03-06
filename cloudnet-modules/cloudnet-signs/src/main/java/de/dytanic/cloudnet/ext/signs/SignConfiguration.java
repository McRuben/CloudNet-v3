package de.dytanic.cloudnet.ext.signs;

import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignConfiguration {

    public static final Type TYPE = new TypeToken<SignConfiguration>() {
    }.getType();

    protected Collection<SignConfigurationEntry> configurations;

    protected Map<String, String> messages;

}