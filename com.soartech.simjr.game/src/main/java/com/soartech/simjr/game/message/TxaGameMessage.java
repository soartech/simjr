package com.soartech.simjr.game.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.soartech.switchboard.message.ExtendableProtoBufMessage;
import com.soartech.txa.proto.TxaGame;

public class TxaGameMessage extends ExtendableProtoBufMessage
{
    public TxaGameMessage() {super(null);}
    public TxaGameMessage(TxaGame.TxaGameMessage msg) {super(msg);}
    public TxaGameMessage(String str) throws InvalidProtocolBufferException {super(TxaGame.TxaGameMessage.parseFrom(str.getBytes()));}
    @Override protected String getProtoClass() { return TxaGame.TxaGameMessage.class.getSimpleName(); }
    public TxaGame.TxaGameMessage get() {return (TxaGame.TxaGameMessage) data;}
}
