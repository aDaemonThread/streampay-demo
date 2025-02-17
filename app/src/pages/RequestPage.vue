<template>
  <q-page class="items-center" style="margin-left: 12%; margin-right: 12%; margin-top: 70px;">
    <div class="items-center text-primary text-h4" style="margin-left: 40%; margin-bottom: 60px;">
      Requests
    </div>
    <q-table
      ref="tableRef"
      title-class="feed-title"
      hide-bottom
      hide-header
      card-style="box-shadow: none;"
      :rows="requests"
      :columns="columns"
      :table-colspan="9"
      row-key="index"
      virtual-scroll
      :virtual-scroll-item-size="48"
      :rows-per-page-options="[0]"
    >
       <template v-slot:body="props">
         <q-tr :props="props" no-hover>
           <q-td  key="requester" :props="props">
             <div style="margin-bottom: 20px; margin-top: 20px;">
               <div class="text-h6">
                 <b>{{ props.row.request.fromUserName }}</b> requested <b> ${{ props.row.request.amount }}</b>
               </div>
               <div class="text-subtitle2">
                 {{ props.row.request.notes }}
               </div>
             </div>
           </q-td>

           <q-td
             key="action"
             :props="props"
           >
             <div class="text-negative">
               <q-btn
                 label="Pay"
                 color="primary"
                 rounded
                 @click="this.$router.push({ path: '/payorrequest/' + props.row.id })" />
             </div>
           </q-td>
         </q-tr>
       </template>
    </q-table>
  </q-page>
</template>

<script lang="ts">
import {defineComponent, ref, unref} from 'vue';
import {useAuth0} from "@auth0/auth0-vue";
import {streamingUrl} from "boot/axios";
import {Buffer} from "buffer";
import {watchEffectOnceAsync} from "@auth0/auth0-vue/src/utils";

export default defineComponent({
  name: 'MainPage',
  setup () {
    const auth0 = useAuth0();

    const tableRef = ref(null);

    const columns = [
      {
        name: 'requester',
        required: true,
        align: 'left',
        field: 'requester',
        format: (val: any) => `${val}`
      },
      { name: 'action', align: 'right', field: 'amount', sortable: true },
    ]

    const requests = ref([] as any);

    return {
      auth0: auth0,
      tableRef,
      columns,
      requests
    }
  },
  async mounted() {
    const accessToken = await this.auth0.getAccessTokenSilently();
    const requestStream = new EventSource(`${streamingUrl}/payment-requests?access_token=${accessToken}`);
    const requests = this.requests;

    requestStream.onopen = function () {
      requests.slice(0);
    }

    requestStream.addEventListener('delete', (event: MessageEvent) => {
      const lastEventId = JSON.parse(event.lastEventId?.toString());
      const key = Buffer.from(lastEventId[0], "base64").toString("utf8");
      const index = requests.findIndex((r: { id: string; }) => r.id === key);
      requests.splice(index, 1);
    }, false);

    requestStream.onmessage = function (event: MessageEvent) {
      const lastEventId = JSON.parse(event.lastEventId);
      const key:string = Buffer.from(lastEventId[0], "base64").toString("utf8");
      const paymentRequest = JSON.parse(event.data)
      requests.push({id: key, request: paymentRequest})
    };
  }
});
</script>
