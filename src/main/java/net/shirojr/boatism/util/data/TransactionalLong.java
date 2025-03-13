package net.shirojr.boatism.util.data;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.Nullable;

public class TransactionalLong extends SnapshotParticipant<Long> {
    private long value;
    @Nullable
    private Runnable onFinalCommit;

    public TransactionalLong(long initialValue) {
        this.value = initialValue;
    }

    public TransactionalLong(long initialValue, @Nullable Runnable onFinalCommit) {
        this(initialValue);
        this.onFinalCommit = onFinalCommit;
    }

    public long get() {
        return this.value;
    }

    public void set(TransactionContext transactionContext, long newValue) {
        updateSnapshots(transactionContext);
        this.value = newValue;
    }

    @Override
    protected Long createSnapshot() {
        return value;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.value = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        if (this.onFinalCommit != null) {
            this.onFinalCommit.run();
        }
    }
}
