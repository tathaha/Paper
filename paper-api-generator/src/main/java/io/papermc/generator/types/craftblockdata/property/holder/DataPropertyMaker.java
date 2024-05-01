package io.papermc.generator.types.craftblockdata.property.holder;

import com.squareup.javapoet.FieldSpec;
import io.papermc.generator.types.craftblockdata.property.holder.appender.DataAppender;
import io.papermc.generator.utils.BlockStateMapping;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public interface DataPropertyMaker extends DataAppender {

    FieldSpec.Builder getOrCreateField(Map<Property<?>, Field> fields);

    Class<?> getIndexClass();

    @Override
    DataHolderType getType();

    String getBaseName();

    static <T extends Property<?>> DataPropertyMaker make(Collection<T> properties, Class<? extends Block> blockClass, BlockStateMapping.FieldDataHolder<T> fieldDataHolder) {
        if (fieldDataHolder.field() == null) {
            return new VirtualDataPropertyWriter<>(fieldDataHolder.virtualField(), properties, blockClass);
        }
        return new DataPropertyWriter<>(fieldDataHolder.field(), properties, blockClass);
    }
}
