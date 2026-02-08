package io.github.vinipx.quantaf.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI-driven generation of complex ISO 20022 (SWIFT) messages from
 * natural language intent descriptions.
 * <p>
 * Supports response caching for deterministic replay in CI pipelines.
 */
public class SmartStub {

    private static final Logger log = LoggerFactory.getLogger(SmartStub.class);

    private static final String SWIFT_SYSTEM_PROMPT = """
            You are an expert in ISO 20022 (SWIFT) message formats. Generate valid XML messages
            based on the user's intent. Follow the ISO 20022 schema strictly.
            Common message types:
            - pacs.008: FI to FI Customer Credit Transfer
            - pacs.009: FI to FI Financial Institution Credit Transfer
            - camt.053: Bank to Customer Statement
            - sese.023: Securities Settlement Transaction Instruction
            Return ONLY the XML content with no explanation.
            """;

    private final LlmProvider llmProvider;
    private final boolean cacheResponses;
    private final Map<String, String> responseCache;

    public SmartStub(LlmProvider llmProvider, boolean cacheResponses) {
        this.llmProvider = llmProvider;
        this.cacheResponses = cacheResponses;
        this.responseCache = new ConcurrentHashMap<>();
    }

    /**
     * Creates a SmartStub with no LLM (template-only mode).
     */
    public SmartStub() {
        this.llmProvider = null;
        this.cacheResponses = true;
        this.responseCache = new ConcurrentHashMap<>();
    }

    /**
     * Generates an ISO 20022 message from a natural language intent.
     *
     * @param intent the description of the desired message
     * @return the ISO 20022 XML message
     */
    public String generateSwiftMessage(String intent) {
        log.info("Generating SWIFT message from intent: '{}'", intent);

        // Check cache first
        if (cacheResponses) {
            String cached = responseCache.get(intent);
            if (cached != null) {
                log.debug("Returning cached SWIFT message for intent");
                return cached;
            }
        }

        String message;
        if (llmProvider != null && llmProvider.isAvailable()) {
            message = llmProvider.complete(SWIFT_SYSTEM_PROMPT, intent);
        } else {
            message = generateTemplateMessage(intent);
        }

        // Cache the response
        if (cacheResponses) {
            responseCache.put(intent, message);
        }

        return message;
    }

    /**
     * Pre-loads a cached response for a given intent.
     * Useful for deterministic test setups.
     */
    public void cacheResponse(String intent, String response) {
        responseCache.put(intent, response);
        log.debug("Cached SWIFT message for intent: '{}'", intent);
    }

    /**
     * Clears the response cache.
     */
    public void clearCache() {
        responseCache.clear();
        log.info("SWIFT message cache cleared");
    }

    /**
     * Returns the number of cached responses.
     */
    public int getCacheSize() {
        return responseCache.size();
    }

    /**
     * Template-based generation for common SWIFT message types.
     */
    private String generateTemplateMessage(String intent) {
        String lower = intent.toLowerCase();

        if (lower.contains("credit transfer") || lower.contains("pacs.008")) {
            return generateCreditTransferTemplate();
        } else if (lower.contains("statement") || lower.contains("camt.053")) {
            return generateStatementTemplate();
        } else if (lower.contains("settlement") || lower.contains("sese.023")) {
            return generateSettlementTemplate();
        }

        log.warn("No template found for intent, using generic credit transfer");
        return generateCreditTransferTemplate();
    }

    private String generateCreditTransferTemplate() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
                  <FIToFICstmrCdtTrf>
                    <GrpHdr>
                      <MsgId>QUANTAF-MSG-001</MsgId>
                      <CreDtTm>2026-01-01T12:00:00</CreDtTm>
                      <NbOfTxs>1</NbOfTxs>
                      <SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>
                    </GrpHdr>
                    <CdtTrfTxInf>
                      <PmtId><InstrId>INSTR-001</InstrId><EndToEndId>E2E-001</EndToEndId></PmtId>
                      <Amt><InstdAmt Ccy="USD">1000.00</InstdAmt></Amt>
                      <Dbtr><Nm>QUANTAF Test Debtor</Nm></Dbtr>
                      <Cdtr><Nm>QUANTAF Test Creditor</Nm></Cdtr>
                    </CdtTrfTxInf>
                  </FIToFICstmrCdtTrf>
                </Document>
                """;
    }

    private String generateStatementTemplate() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.053.001.02">
                  <BkToCstmrStmt>
                    <GrpHdr><MsgId>QUANTAF-STMT-001</MsgId></GrpHdr>
                    <Stmt>
                      <Id>STMT-001</Id>
                      <Bal><Tp><CdOrPrtry><Cd>OPBD</Cd></CdOrPrtry></Tp>
                        <Amt Ccy="USD">50000.00</Amt></Bal>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;
    }

    private String generateSettlementTemplate() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:sese.023.001.01">
                  <SctiesSttlmTxInstr>
                    <TxId>QUANTAF-STTL-001</TxId>
                    <SttlmTpAndAddtlParams>
                      <SttlmTp>DVP</SttlmTp>
                    </SttlmTpAndAddtlParams>
                    <QtyAndAcctDtls>
                      <SttlmQty><Qty><Unit>1000</Unit></Qty></SttlmQty>
                    </QtyAndAcctDtls>
                  </SctiesSttlmTxInstr>
                </Document>
                """;
    }
}
