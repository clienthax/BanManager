/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.confuser.banmanager.common.sender;

import com.google.common.base.Splitter;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.util.Location;
import me.confuser.banmanager.util.TextUtils;
import net.kyori.text.Component;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Simple implementation of {@link Sender} using a {@link SenderFactory}
 *
 * @param <T> the command sender type
 */
public final class AbstractSender<T> implements Sender {
    private static final Splitter NEW_LINE_SPLITTER = Splitter.on("\n");

    private final BanManagerPlugin platform;
    private final SenderFactory<T> factory;
    private final WeakReference<T> reference;

    private final UUID uuid;
    private final String name;

    AbstractSender(BanManagerPlugin platform, SenderFactory<T> factory, T t) {
        this.platform = platform;
        this.factory = factory;
        this.reference = new WeakReference<>(t);
        this.uuid = factory.getUuid(t);
        this.name = factory.getName(t);
    }

    @Override
    public BanManagerPlugin getPlugin() {
        return this.platform;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.name;//TODO
    }

    @Override
    public void sendMessage(String message) {
        final T t = this.reference.get();
        if (t != null) {

            if (!isConsole()) {
                this.factory.sendMessage(t, message);
                return;
            }

            // if it is console, split up the lines and send individually.
            for (String line : NEW_LINE_SPLITTER.split(message)) {
                this.factory.sendMessage(t, line);
            }
        }
    }

    @Override
    public void sendMessage(Component message) {
        if (isConsole()) {
            sendMessage(TextUtils.toLegacy(message));
            return;
        }

        final T t = this.reference.get();
        if (t != null) {
            this.factory.sendMessage(t, message);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        T t = this.reference.get();
        if (t != null) {
            if (this.factory.hasPermission(t, permission)) {
                return true;
            }
        }

        return isConsole();
    }

    @Override
    public boolean isValid() {
        return this.reference.get() != null;
    }

    @Override
    public InetAddress getIPAddress() {
        T t = this.reference.get();
        if (t != null) {
            return this.factory.getIPAddress(t);
        }
        return null;
    }

    @Override
    public void kick(String message) {
        T t = this.reference.get();
        if (t != null) {
            this.factory.kick(t, message);
        }
    }

    @Override
    public void teleport(Location location) {

    }

    @Override
    public boolean isInsideVehicle() {
        T t = this.reference.get();
        if (t != null) {
            return this.factory.isInsideVehicle(t);
        }

        return false;
    }

    @Override
    public void leaveVehicle() {
        T t = this.reference.get();
        if (t != null) {
            this.factory.leaveVehicle(t);
        }
    }

    @Override
    public Location getLocation() {
        T t = this.reference.get();
        if (t != null) {
            return this.factory.getLocation(t);
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof AbstractSender)) return false;
        final AbstractSender that = (AbstractSender) o;
        return this.getUuid().equals(that.getUuid());
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }
}