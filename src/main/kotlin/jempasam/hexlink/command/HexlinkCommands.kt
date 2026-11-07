package jempasam.hexlink.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import jempasam.hexlink.HexlinkRegistry
import jempasam.hexlink.world.HexlinkLevelData
import jempasam.hexlink.world.LevelRanks
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.CommandSelection
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation


object HexlinkCommands {

    /** Called from NeoForge's `RegisterCommandsEvent`. */
    fun registerCommands(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        environment: CommandSelection,
        registryAccess: CommandBuildContext
    ) {
        commandRegistration(dispatcher, registryAccess, environment)
    }

    /**
     * Resolve a rank by its registry id.
     *
     * Ranks live in [HexlinkRegistry.RANK], which is a Hexlink-owned table rather than a
     * NeoForge registry, so the argument is a plain string completed from that table.
     */
    private val INVALID_RANK = SimpleCommandExceptionType(Component.translatable("argument.id.invalid"))

    fun getRank(name: String): LevelRanks.Rank {
        val id = ResourceLocation.tryParse(name) ?: throw INVALID_RANK.create()
        return HexlinkRegistry.RANK.get(id) ?: throw INVALID_RANK.create()
    }

    private fun rankArgument() = Commands.argument("rank", StringArgumentType.string())
        .suggests { _, builder ->
            SharedSuggestionProvider.suggestResource(
                HexlinkRegistry.RANK.entrySet.map { it.key.location() },
                builder
            )
        }

    private fun rankOf(context: CommandContext<CommandSourceStack>): LevelRanks.Rank =
        getRank(StringArgumentType.getString(context, "rank"))

    private fun commandRegistration(dispatcher: CommandDispatcher<CommandSourceStack>, registryAccess: CommandBuildContext, environment: CommandSelection) {
        dispatcher.register(literal("magicrank")
            .requires { it.hasPermission(3) }
            .then(literal("rank")
                .then(rankArgument()
                    .then(literal("owner")
                        .then(literal("get")
                            .executes {
                                val rank = rankOf(it)
                                val data = HexlinkLevelData.get(it.source.level)
                                val player = data.getPlayer(rank)
                                if (player != null) {
                                    val name = it.source.server.playerList.getPlayer(player)
                                        ?.name
                                        ?: Component.literal(player.toString())
                                    it.source.sendSuccess(
                                        {
                                            name.copy()
                                                .append(Component.translatable("hexlink.text.is"))
                                                .append(rank.getName())
                                        },
                                        false
                                    )
                                    1
                                } else {
                                    it.source.sendSuccess({ Component.translatable("hexlink.no_owner") }, false)
                                    0
                                }
                            }
                        )
                        .then(literal("set")
                            .then(Commands.argument("player", EntityArgument.player())
                                .executes {
                                    val rank = rankOf(it)
                                    val player = EntityArgument.getPlayer(it, "player")
                                    val data = HexlinkLevelData.get(it.source.level)
                                    data.setRank(player.uuid, rank)
                                    0
                                }
                            )
                        )
                        .then(literal("clear")
                            .executes {
                                val rank = rankOf(it)
                                val data = HexlinkLevelData.get(it.source.level)
                                data.setPlayer(rank, null)
                                0
                            }
                        )
                    )
                )
            )
            .then(literal("ranks")
                .executes {
                    val context = it
                    val data = HexlinkLevelData.get(context.source.level)
                    val text = Component.empty()
                    for (rank in data.ranks()) {
                        val player = data.getPlayer(rank)
                        text.append(rank.getName())
                        if (player != null) {
                            val name = context.source.server.playerList.getPlayer(player)
                                ?.name
                                ?: Component.literal(player.toString())
                            text.append(" : ").append(name)
                        }
                        text.append("\n")
                    }
                    context.source.sendSuccess({ text }, false)
                    0
                }
            )
        )
    }
}
