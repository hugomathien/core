package eventprocessors;

public enum CoreEventType implements EventType {
DATA_REQUEST,
SPOT,
BAR,
DAY,
TICK,
TRADE,
QUOTE,
SPLIT,
DIVIDEND,
TIMER,
NEW_INSTRUMENT,
DELIST_INSTRUMENT,
UPDATE_INSTRUMENT,
TRADING_SESSION_START,
TRADING_SESSION_END,
CALCULATOR,
CLIENT_ORDER,
RAVEN_PACK,
TWO_IQ,
ALEXANDRIA,
BLOCK_TRADE,
NQP_INSTRUMENT,
NQP_FLOW,
PORTFOLIO_COMPOSITION
}
