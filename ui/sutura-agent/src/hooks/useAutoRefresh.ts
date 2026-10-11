import {useEffect,useRef} from "react";
import {AppState,Platform} from "react-native";
/** Refresh read-only data while visible; serialize automatic refreshes. */
export function useAutoRefresh(refresh:()=>Promise<void>,enabled:boolean,intervalMs=30000){
 const callback=useRef(refresh);callback.current=refresh;
 useEffect(()=>{if(!enabled)return;let stopped=false;let running=false;
 const visible=()=>AppState.currentState==="active"&&(Platform.OS!=="web"||typeof document==="undefined"||document.visibilityState==="visible");
 const run=async()=>{if(stopped||running||!visible())return;running=true;try{await callback.current();}catch{/* The data owner reports errors. */}finally{running=false;}};
 const subscription=AppState.addEventListener("change",state=>{if(state==="active")void run();});
 const focus=()=>{void run();};
 if(Platform.OS==="web"&&typeof window!=="undefined"){window.addEventListener("focus",focus);document.addEventListener("visibilitychange",focus);}
 const timer=setInterval(()=>void run(),intervalMs);
 return()=>{stopped=true;clearInterval(timer);subscription.remove();if(Platform.OS==="web"&&typeof window!=="undefined"){window.removeEventListener("focus",focus);document.removeEventListener("visibilitychange",focus);}};
 },[enabled,intervalMs]);
}
