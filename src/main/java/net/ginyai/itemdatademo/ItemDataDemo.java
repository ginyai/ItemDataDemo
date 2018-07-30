package net.ginyai.itemdatademo;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStateEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

import java.util.Optional;

@Plugin(
        id = "itemdatademo",
        name = "ItemDataDemo"
)
public class ItemDataDemo {

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer pluginContainer;

    public static Key<Value<Integer>> KEY = DummyObjectProvider.createExtendedFor(Key.class, "KEY");

    @Listener
    public void onGameState(GameStateEvent event){
        logger.info("on {}",event.getState());
    }

    @Listener
    public void onRegisterKey(GameRegistryEvent.Register<Key<?>> event){
        logger.info("on register key");
        KEY = Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN)
                .id("simple_initeger")
                .name("SimpleInteger")
                .query(DataQuery.of("SimpleInteger"))
                .build();
        event.register(KEY);
    }

    @Listener
    public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?,?>> event){
        logger.info("on register data");
        DataRegistration.<SimpleIntegerData,SimpleIntegerData.Immutable>builder()
                .dataClass(SimpleIntegerData.class)
                .immutableClass(SimpleIntegerData.Immutable.class)
                .builder(new SimpleIntegerData.Builder())
                .dataName("SimpleInteger")
                .manipulatorId("simple_integer")
                .buildAndRegister(pluginContainer);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        CommandSpec set = CommandSpec.builder()
                .executor((src, args) -> {
                    if(!(src instanceof Player)){
                        throw new CommandException(Text.of("Player Only"));
                    }
                    Player player = (Player) src;
                    ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND)
                            .orElseThrow(()->new CommandException(Text.of("No item in main hand.")));
                    SimpleIntegerData data = itemStack.getOrCreate(SimpleIntegerData.class).get();
                    data.set(KEY,args.<Integer>getOne("int").get());
                    DataTransactionResult result = itemStack.offer(data);
                    if(result.isSuccessful()){
                        player.setItemInHand(HandTypes.MAIN_HAND,itemStack);
                        src.sendMessage(Text.of(TextColors.GREEN,"Succeed."));
                        return CommandResult.success();
                    }else {
                        src.sendMessage(Text.of(TextColors.RED,"Failed."));
                        src.sendMessage(Text.of(result.toString()));
                        return CommandResult.empty();
                    }
                }).build();
        CommandSpec check = CommandSpec.builder()
                .arguments(GenericArguments.integer(Text.of("int")))
                .executor((src, args) -> {
                    if(!(src instanceof Player)){
                        throw new CommandException(Text.of("Player Only"));
                    }
                    Player player = (Player) src;
                    ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND)
                            .orElseThrow(()->new CommandException(Text.of("No item in main hand.")));
                    Optional<Integer> optionalInteger = itemStack.get(KEY);
                    if(optionalInteger.isPresent()){
                        src.sendMessage(Text.of("Value:",TextColors.GREEN,optionalInteger.get()));
                    }else {
                        src.sendMessage(Text.of(TextColors.YELLOW,"No Value Found."));
                    }
                    return CommandResult.success();
                }).build();
        CommandSpec main = CommandSpec.builder()
                .child(set,"set")
                .child(check,"check")
                .build();

        Sponge.getCommandManager().register(this,main,"itemdatademo");
    }
}
