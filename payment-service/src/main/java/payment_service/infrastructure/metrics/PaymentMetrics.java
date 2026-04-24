package payment_service.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import payment_service.infrastructure.event.PaymentCommand;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;
    private final Map<String, ActiveCommand> activeCommands = new ConcurrentHashMap<>();

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void commandReceived(PaymentCommand.PaymentCommandType type) {
        counter("shipping_payment_commands_received_total", type.name()).increment();
    }

    public void commandRejected(PaymentCommand.PaymentCommandType type) {
        counter("shipping_payment_commands_rejected_total", type.name()).increment();
    }

    public void commandStarted(PaymentCommand.PaymentCommandType type, String paymentId, Instant receivedAt) {
        activeCommands.put(paymentId, new ActiveCommand(type.name(), receivedAt != null ? receivedAt : Instant.now()));
    }

    public void commandCompleted(String paymentId, String outcome) {
        ActiveCommand activeCommand = activeCommands.remove(paymentId);
        if (activeCommand == null) {
            return;
        }

        List<Tag> tags = List.of(
                Tag.of("command", activeCommand.commandType()),
                Tag.of("outcome", outcome)
        );

        Counter.builder("shipping_payment_commands_completed_total")
                .tags(tags)
                .register(meterRegistry)
                .increment();

        Timer.builder("shipping_payment_command_duration")
                .description("End-to-end latency from payment command reception to terminal payment event")
                .tags(tags)
                .register(meterRegistry)
                .record(Duration.between(activeCommand.startedAt(), Instant.now()));
    }

    private Counter counter(String name, String commandType) {
        return Counter.builder(name)
                .tag("command", commandType)
                .register(meterRegistry);
    }

    private record ActiveCommand(String commandType, Instant startedAt) {
    }
}
