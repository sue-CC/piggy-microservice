package com.piggy.microservice.notification.repository.converter;

import com.piggy.microservice.notification.domain.Frequency;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FrequencyReaderConverter implements Converter<Integer, Frequency> {

	@Override
	public Frequency convert(Integer seconds) {
		return Frequency.withSeconds(seconds);
	}
}
