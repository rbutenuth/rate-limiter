package de.codecentric.limiter.api;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.sdk.api.metadata.MetadataContext;
import org.mule.sdk.api.metadata.resolving.OutputTypeResolver;

public class SetAttributesOutputResolver  implements OutputTypeResolver<Object>{
	@Override
	public String getCategoryName() {
		return "SetAttributesCategory";
	}

	@Override
	public MetadataType getOutputType(MetadataContext context, @SuppressWarnings("unused") Object unusedKey) {
		BaseTypeBuilder typeBuilder = context.getTypeBuilder();
		ObjectTypeBuilder record = typeBuilder.objectType();
		return record.build();
	}
}
