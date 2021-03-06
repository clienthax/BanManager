package me.confuser.banmanager.events;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.api.event.Cancellable;

public abstract class CustomCancellableEvent extends CustomEvent implements Cancellable {

  @Getter
  @Setter
  private boolean cancelled = false;

  public CustomCancellableEvent(boolean isAsync) {
    super(isAsync);
  }
}
