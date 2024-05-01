package io.papermc.generator.types.craftblockdata.property.holder;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.papermc.generator.types.StructuredGenerator;
import io.papermc.generator.types.craftblockdata.property.converter.ConverterBase;
import io.papermc.generator.utils.BlockStateMapping;
import io.papermc.generator.utils.NamingManager;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

public class VirtualDataPropertyWriter<T extends Property<?>> extends DataPropertyWriterBase<T> {

    private final VirtualField<T> fieldInfo;
    protected Class<?> indexClass;
    protected TypeName fieldType;

    protected VirtualDataPropertyWriter(VirtualField<T> fieldInfo, Collection<T> properties, Class<? extends Block> blockClass) {
        super(properties, blockClass);
        this.fieldInfo = fieldInfo;
        this.computeTypes(fieldInfo);
    }

    protected void computeTypes(VirtualField<T> fieldInfo) {
        switch (fieldInfo.getHolderType()) {
            case ARRAY -> {
                this.indexClass = Integer.TYPE;
                this.fieldType = ArrayTypeName.of(fieldInfo.getValueType());
            }
            case LIST -> {
                this.indexClass = Integer.TYPE;
                this.fieldType = ParameterizedTypeName.get(List.class, fieldInfo.getValueType());
            }
            case MAP -> {
                if (fieldInfo.getKeyClass() != null) {
                    this.indexClass = fieldInfo.getKeyClass();
                } else {
                    this.indexClass = this.properties.iterator().next().getValueClass();
                    if (this.indexClass.isEnum()) {
                        this.indexClass = BlockStateMapping.ENUM_BRIDGE.getOrDefault(this.indexClass, (Class<? extends Enum<?>>) this.indexClass);
                    }
                }
                this.fieldType = ParameterizedTypeName.get(Map.class, this.indexClass, fieldInfo.getValueType());
            }
        }
    }

    @Override
    public FieldSpec.Builder getOrCreateField(Map<Property<?>, Field> fields) {
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(this.fieldType, this.fieldInfo.getName(), PRIVATE, STATIC, FINAL);
        if (this.getType() == DataHolderType.ARRAY || this.getType() == DataHolderType.LIST) {
            CodeBlock.Builder code = CodeBlock.builder();
            this.createSyntheticCollection(code, this.getType() == DataHolderType.ARRAY, fields);
            fieldBuilder.initializer(code.build());
        } else if (this.getType() == DataHolderType.MAP) {
            CodeBlock.Builder code = CodeBlock.builder();
            this.createSyntheticMap(code, this.indexClass, fields);
            fieldBuilder.initializer(code.build());
        }

        return fieldBuilder;
    }

    @Override
    public Class<?> getIndexClass() {
        return this.indexClass;
    }

    @Override
    public DataHolderType getType() {
        return this.fieldInfo.getHolderType();
    }

    @Override
    public String getBaseName() {
        return this.fieldInfo.getBaseName();
    }

    @Override
    public void addExtras(final TypeSpec.Builder builder, final FieldSpec field, final ParameterSpec indexParameter, final ConverterBase converter, final StructuredGenerator<?> generator, final NamingManager naming) {

    }
}
