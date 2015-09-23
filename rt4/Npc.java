package org.powerbot.script.rt4;

import java.awt.Color;

import org.powerbot.bot.rt4.HashTable;
import org.powerbot.bot.rt4.client.Cache;
import org.powerbot.bot.rt4.client.Client;
import org.powerbot.bot.rt4.client.NpcConfig;
import org.powerbot.bot.rt4.client.Varbit;
import org.powerbot.script.Actionable;
import org.powerbot.script.Identifiable;

public class Npc extends Actor implements Identifiable, Actionable {
	public static final Color TARGET_COLOR = new Color(255, 0, 255, 15);
	private final org.powerbot.bot.rt4.client.Npc npc;
	private static final int[] lookup;
	private final int hash;

	static {
		lookup = new int[32];
		int i = 2;
		for (int j = 0; j < 32; j++) {
			lookup[j] = i - 1;
			i += i;
		}
	}

	Npc(final ClientContext ctx, final org.powerbot.bot.rt4.client.Npc npc) {
		super(ctx);
		this.npc = npc;
		hash = System.identityHashCode(npc);
	}

	@Override
	protected org.powerbot.bot.rt4.client.Actor getActor() {
		return npc;
	}

	@Override
	public String name() {
		final NpcConfig config = getConfig();
		final String str = config != null ? config.getName() : "";
		return str != null ? str : "";
	}

	@Override
	public int combatLevel() {
		final NpcConfig config = getConfig();
		return config != null ? config.getLevel() : -1;
	}

	@Override
	public int id() {
		final Client client = ctx.client();
		if (client == null) {
			return -1;
		}
		final NpcConfig config = npc != null ? npc.getConfig() : null;
		if (config != null) {
			final int varbit = config.getVarbit(), si = config.getVarpbitIndex();
			int index = -1;
			if (varbit != -1) {
				final Cache cache = client.getVarbitCache();
				final Varbit varBit = HashTable.lookup(cache.getTable(), varbit, Varbit.class);
				if (varBit.obj.get() != null) {
					final int mask = lookup[varBit.getEndBit() - varBit.getStartBit()];
					index = ctx.varpbits.varpbit(varBit.getIndex()) >> varBit.getStartBit() & mask;
				}
			} else if (si != -1) {
				index = ctx.varpbits.varpbit(si);
			}
			if (index >= 0) {
				final int[] configs = config.getConfigs();
				if (configs != null && index < configs.length && configs[index] != -1) {
					return configs[index];
				}
			}
			return config.getId();
		}
		return -1;
	}

	@Override
	public String[] actions() {
		final NpcConfig config = getConfig();
		final String[] arr = config != null ? config.getActions() : new String[0];
		if (arr == null) {
			return new String[0];
		}
		final String[] arr_ = new String[arr.length];
		int c = 0;
		for (final String str : arr) {
			arr_[c++] = str != null ? str : "";
		}
		return arr_;
	}

	private NpcConfig getConfig() {
		final Client client = ctx.client();
		final NpcConfig config = npc != null ? npc.getConfig() : null;
		if (client == null || config == null) {
			return null;
		}
		final int id = config.getId(), uid = id();
		if (id != uid) {
			final NpcConfig c = HashTable.lookup(client.getNpcConfigCache().getTable(), uid, NpcConfig.class);
			if (c != null) {
				return c;
			}
		}
		return config;
	}

	@Override
	public boolean valid() {
		final Client client = ctx.client();
		if (client == null || npc.obj.get() == null) {
			return false;
		}
		final org.powerbot.bot.rt4.client.Npc[] arr = client.getNpcs();
		for (final org.powerbot.bot.rt4.client.Npc a : arr) {
			if (npc.equals(a)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return String.format("%s[id=%d/name=%s/level=%d]",
				Npc.class.getName(), id(), name(), combatLevel());
	}
}