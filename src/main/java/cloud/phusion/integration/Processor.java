package cloud.phusion.integration;

/**
 * Processor of one step in an integration.
 */
public interface Processor {

    void process(Transaction trx) throws Exception;

}
