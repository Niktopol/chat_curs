"use client"

import Image from "next/image";
import chatStyles from "./modules/chat.module.css";
import chatPanelStyles from "./modules/chat_panel.module.css";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import ChatHeader from "./chat_header";
import { useDispatch, useSelector } from "react-redux";
import { setChat } from "@/store/chatSelectedSlice";
import Messages from "./messages";
import MessageInput from "./message_input";

export function Chat(){
    const chat = useSelector((state) => state.chatSelected);

    return (
        <div className={chatStyles.main}>
            { chat.id ? (
                <>
                    <ChatHeader></ChatHeader>
                    <Messages></Messages>
                    <MessageInput></MessageInput>
                </>
                ) : null }
        </div>
    );
}

export function ChatPanel({ data }){
    const [lastMsg, setLastMsg] = useState("");
    const [chatPic, setChatPic] = useState("/default_user.svg");
    const [currentName, setCurrentName] = useState(data.name);
    const websocket = useSelector((state) => state.websocketMessage);
    const dispatch = useDispatch();
    const router = useRouter();

    useEffect(() => {
        return () => {
            if (chatPic.startsWith('blob:')) {
                URL.revokeObjectURL(chatPic);
            }
        }
    }, [chatPic]);

    useEffect(() => {
        setCurrentName(data.name);
    }, [data.name])

    const chatPicFetch = async () => {
        try {
            const chatpic_resp = await fetch(`http://localhost:8080/chats/image/${encodeURIComponent(data.id)}`, {credentials: "include"});

            if (chatpic_resp.ok) {
                const blob = await chatpic_resp.blob();
                setChatPic(URL.createObjectURL(blob));
            }
        } catch (e) {
            router.push("/login");
        }  
    }

    const userPicFetch = async () => {
        try {
            const userpic_resp = await fetch(`http://localhost:8080/user/profilepic/${encodeURIComponent(data.username)}`, {credentials: "include"});

            if (userpic_resp.ok) {
                const blob = await userpic_resp.blob();
                setChatPic(URL.createObjectURL(blob));
            }
        } catch (e) {
            router.push("/login");
        }
    }

    const userInfoFetch = async () => {
        try {
            const user_resp = await fetch(`http://localhost:8080/user/profile/${encodeURIComponent(data.username)}`, {credentials: "include"});

            if (user_resp.ok) {
                setCurrentName((await user_resp.json()).name);
            } else {
                throw new Error((await user_resp.json())?.error);
            }
        } catch (e) {
            router.push("/login");
        }
    }

    const fetchLastMsg = async () => {
        try {
            const msg_resp = await fetch(`http://localhost:8080/messages/last/${data.id}`, {credentials: "include"});

            if (msg_resp.ok) {
                const msg = await msg_resp.json();
                if (msg.attachment) {
                    setLastMsg("Изображение");
                } else {
                    setLastMsg(msg.text);
                }
            } else {
                setLastMsg("");
            }
        } catch (e) {
            router.push("/login");
        }
    }

    useEffect(() => {
        if (data.private) {
            userPicFetch();
        } else {
            chatPicFetch();
        }
        if (!data.new) {
            fetchLastMsg();
        }
    }, [data.id, data.username]);

    useEffect(() => {
        if (!data.private && websocket.message?.title === "Chat image updated" && websocket.message.id == data.id) {
            chatPicFetch();
        }
        if (!data.new && data.private && websocket.message?.title === "User info updated" && websocket.message.username == data.username) {
            userPicFetch();
            userInfoFetch();
        }

        if (websocket.message?.title === "Message received" && websocket.message.id == data.id){
            fetchLastMsg();
        }
    }, [websocket])

    const selectChat = async () => {
        dispatch(setChat(data));
    }

    return (
        <div onClick={selectChat} className={chatPanelStyles.main}>
            <div className={chatPanelStyles.info}>
                <div className={chatPanelStyles.image}>
                    <Image src={chatPic} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                </div>
                <div className={chatPanelStyles.title_wrapper}>
                    <p className={`${chatPanelStyles.meta} ${chatPanelStyles.id}`}>{data.private ? <><span>@</span>{data.username}</> : ""}</p>
                    <p className={chatPanelStyles.title}>{currentName}</p>
                    <p className={`${chatPanelStyles.meta} ${chatPanelStyles.last_msg}`}>{lastMsg}</p>
                </div>
            </div>
        </div>
    );
}