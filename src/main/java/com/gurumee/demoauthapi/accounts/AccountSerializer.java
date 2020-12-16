package com.gurumee.demoauthapi.accounts;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class AccountSerializer extends JsonSerializer<Account> {
    @Override
    public void serialize(Account account, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", account.getId());
        jsonGenerator.writeStringField("username", account.getUsername());
        jsonGenerator.writeEndObject();
    }
}
