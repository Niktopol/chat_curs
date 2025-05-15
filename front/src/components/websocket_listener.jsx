"use client"

import { setMessage } from "@/store/websocketMessageSlice";
import { Client } from "@stomp/stompjs";
import { useRouter } from "next/navigation";
import { useEffect, useRef } from "react";
import { useDispatch } from "react-redux";
import SockJS from "sockjs-client";

export default function WebSocketListener({ topic = "/user/queue/messages" }) {
    const stompClientRef = useRef(null);
    const dispatch = useDispatch();
    const router = useRouter()

    const onMessage = (msg) => {
        console.log(msg);
        dispatch(setMessage(msg))
    };

    useEffect(() => {
        const socketFactory = () => new SockJS("http://localhost:8080/webs");

        const stompClient = new Client({
            webSocketFactory: socketFactory,
            onConnect: () => {
                stompClient.subscribe(topic, (message) => {
                    if (message.body) {
                        onMessage(JSON.parse(message.body));
                    }
                });
            },
            onStompError: (frame) => {
                router.push("/login");;
            },
            onWebSocketClose: (event) => {
                router.push("/login");
            },
            onDisconnect: () => {
                router.push("/login");
            },
            debug: () => {}
        });

        stompClient.activate();
        stompClientRef.current = stompClient;

        return () => {
            if (stompClientRef.current && stompClientRef.current.active) {
                stompClientRef.current.deactivate();
            }
        };
    }, [topic]);

    return null;
}