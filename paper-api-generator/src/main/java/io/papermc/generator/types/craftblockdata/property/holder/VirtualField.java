package io.papermc.generator.types.craftblockdata.property.holder;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import net.minecraft.world.level.block.state.properties.Property;
import java.lang.reflect.Type;
import java.util.List;

public class VirtualField<T extends Property<? extends Comparable<?>>> {

    private final String name;
    private final Type valueType;
    private final DataHolderType holderType;
    private final String baseName;
    private final Class<?> keyClass;
    private final List<T> values;

    private VirtualField(String name, TypeToken<T> valueType, DataHolderType holderType, String baseName, Class<?> keyClass, List<T> values) {
        this.name = name;
        this.valueType = valueType.getType();
        this.holderType = holderType;
        this.baseName = baseName;
        this.keyClass = keyClass;
        this.values = values;
    }

    public static <T extends Property<? extends Comparable<?>>> VirtualField.FieldValue<T>  createCollection(String name, TypeToken<T> valueType, boolean isArray, String baseName) {
        return new VirtualField.FieldValue<>(name, valueType, isArray ? DataHolderType.ARRAY : DataHolderType.LIST, baseName, null);
    }

    public static <T extends Property<? extends Comparable<?>>> VirtualField.FieldValue<T> createMap(String name, Class<?> keyClass, TypeToken<T> valueType, String baseName) {
        return new VirtualField.FieldValue<>(name, valueType, DataHolderType.MAP, baseName, keyClass);
    }

    public static <T extends Property<? extends Comparable<?>>> VirtualField.FieldValue<T> createMap(String name, Class<?> keyClass, Class<T> valueType, String baseName) {
        return createMap(name, keyClass, TypeToken.of(valueType), baseName);
    }

    public String getName() {
        return this.name;
    }

    public Type getValueType() {
        return this.valueType;
    }

    public String getBaseName() {
        return this.baseName;
    }

    public DataHolderType getHolderType() {
        return this.holderType;
    }

    public Class<?> getKeyClass() {
        return this.keyClass;
    }

    public List<T> getValues() {
        return this.values;
    }

    public static class FieldValue<T extends Property<? extends Comparable<?>>> {

        private final String name;
        private final DataHolderType holderType;
        private final TypeToken<T> valueType;
        private final String baseName;
        private final Class<?> keyClass;

        private List<T> values;

        public FieldValue(String name, TypeToken<T> valueType, DataHolderType holderType, String baseName, Class<?> keyClass) {
            this.name = name;
            this.valueType = valueType;
            this.holderType = holderType;
            this.baseName = baseName;
            this.keyClass = keyClass;
        }

        public FieldValue<T> withValues(List<T> properties) {
            this.values = List.copyOf(properties);
            return this;
        }

        public VirtualField<T> make() {
            Preconditions.checkState(this.values != null && !this.values.isEmpty(), "The field should doesn't have any content");
            return new VirtualField<>(this.name, this.valueType, this.holderType, this.baseName, this.keyClass, this.values);
        }
    }
}
