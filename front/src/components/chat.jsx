"use client"

import Image from "next/image";
import chatStyles from "./modules/chat.module.css";
import chatPanelStyles from "./modules/chat_panel.module.css";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import ChatHeader from "./chat_header";
import { useDispatch, useSelector } from "react-redux";
import { setChat } from "@/store/chatSelectedSlice";

export function Chat(){
    const chat = useSelector((state) => state.chatSelected);

    return (
        <div className={chatStyles.main}>
            { chat.id ? <ChatHeader id={chat.private} username={chat.username} name={chat.name} isPrivate={chat.private}></ChatHeader> : null }
        </div>
    );
}

export function ChatPanel({ data }){
    const [lastMsg, setLastMsg] = useState("");
    const [chatPic, setChatPic] = useState("/default_user.svg");
    const dispatch = useDispatch();
    const router = useRouter();

    useEffect(() => {
        return () => {
            if (chatPic.startsWith('blob:')) {
                if (chatPic) URL.revokeObjectURL(chatPic);
            }
        }
    }, [chatPic]);

    useEffect(() => {
        const chatPicFetch = async () => {
            const chatpic_resp = await fetch(`http://localhost:8080/chats/image/${encodeURIComponent(data.id)}`, {credentials: "include"});

            if (chatpic_resp.ok) {
                const blob = await chatpic_resp.blob();
                setChatPic(URL.createObjectURL(blob));
            }
        }

        const userPicFetch = async () => {
            const userpic_resp = await fetch(`http://localhost:8080/user/profilepic/${encodeURIComponent(data.username)}`, {credentials: "include"});

            if (userpic_resp.ok) {
                const blob = await userpic_resp.blob();
                setChatPic(URL.createObjectURL(blob));
            }
        }
        try {
            if (data.new) {
                userPicFetch();
            } else {
                chatPicFetch();
            }
        } catch (e) {
            router.push("/login");
        }
    }, [data]);

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
                    <p className={chatPanelStyles.title}>{data.name}</p>
                    <p className={`${chatPanelStyles.meta} ${chatPanelStyles.last_msg}`}>{lastMsg}</p>
                </div>
            </div>
        </div>
    );
}