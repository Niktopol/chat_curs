import Chats from "@/components/chats";
import styles from "./page.module.css";
import { Chat } from "@/components/chat";
import FetchUserSession from "@/components/fetch_user_session";
import LoadingPanel from "@/components/loading_panel";
import WebSocketListener from "@/components/websocket_listener";

export default function Home() {
return (
    <>
      <LoadingPanel session_required={true}></LoadingPanel>
      <main className={styles.main}>
          <FetchUserSession mustHaveSession={true} redirect={"/login"}></FetchUserSession>
          <WebSocketListener></WebSocketListener>
          <Chats></Chats>
          <Chat></Chat>
      </main>
    </>
  );
}
