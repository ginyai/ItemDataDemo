package net.ginyai.itemdatademo;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
public class SimpleIntegerData extends AbstractSingleData<Integer,SimpleIntegerData,SimpleIntegerData.Immutable> {

    public static final int CONTENT_VERSION = 1;

    SimpleIntegerData(Integer value) {
        super(value, ItemDataDemo.KEY);
    }

    @Override
    protected Value<?> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(ItemDataDemo.KEY,getValue());
    }

    @Override
    public Optional<SimpleIntegerData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<SimpleIntegerData> dataIn = dataHolder.get(SimpleIntegerData.class);
        if (dataIn.isPresent()) {
            SimpleIntegerData finalData = overlap.merge(this, dataIn.get());
            setValue(finalData.getValue());
        }
        return Optional.of(this);
    }

    @Override
    public Optional<SimpleIntegerData> from(DataContainer container) {
        return from((DataView)container);
    }

    public Optional<SimpleIntegerData> from(DataView dataView) {
        if(dataView.contains(ItemDataDemo.KEY.getQuery())){
            setValue(dataView.getInt(ItemDataDemo.KEY.getQuery()).get());
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Override
    public SimpleIntegerData copy() {
        return new SimpleIntegerData(getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(getValue());
    }

    @Override
    public int getContentVersion() {
        return CONTENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(ItemDataDemo.KEY.getQuery(),getValue());
    }

    public static class Immutable extends AbstractImmutableSingleData<Integer,Immutable,SimpleIntegerData> {

        Immutable(Integer value) {
            super(value, ItemDataDemo.KEY);
        }

        @Override
        protected ImmutableValue<?> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(ItemDataDemo.KEY,getValue()).asImmutable();
        }

        @Override
        public SimpleIntegerData asMutable() {
            return new SimpleIntegerData(getValue());
        }

        @Override
        public int getContentVersion() {
            return CONTENT_VERSION;
        }

        @Override
        public DataContainer toContainer() {
            return super.toContainer()
                    .set(ItemDataDemo.KEY.getQuery(),getValue());
        }
    }


    public static class Builder extends AbstractDataBuilder<SimpleIntegerData>
            implements DataManipulatorBuilder<SimpleIntegerData,SimpleIntegerData.Immutable> {


        Builder() {
            super(SimpleIntegerData.class, CONTENT_VERSION);
        }

        @Override
        public SimpleIntegerData create() {
            return new SimpleIntegerData(-1);
        }

        @Override
        public Optional<SimpleIntegerData> createFrom(DataHolder dataHolder) {
            return create().fill(dataHolder);
        }

        @Override
        protected Optional<SimpleIntegerData> buildContent(DataView container) throws InvalidDataException {
            return create().from(container);
        }
    }
}
